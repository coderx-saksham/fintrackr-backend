package in.bushansirgur.moneymanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.bushansirgur.moneymanager.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final GeminiService geminiService;
    private final DashboardService dashboardService;
    private final CategoryService categoryService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiInsightsResponseDTO getInsights() {
        Map<String, Object> context = buildFinancialContext();
        String prompt = """
                You are a personal finance advisor for an Indian user.
                Financial data (JSON): %s

                Write a personalized analysis with these sections:
                1. Spending overview (top categories, patterns)
                2. Income overview
                3. Balance health (income vs expense)
                4. 2-3 actionable savings tips

                Use INR (₹). Use bullet points. Keep under 200 words. Be friendly.
                If data is empty, encourage the user to add transactions.
                """.formatted(context);

        String insights = geminiService.generateText(prompt);
        return AiInsightsResponseDTO.builder().insights(insights).build();
    }

    public ParseExpenseResponseDTO parseExpense(ParseExpenseRequestDTO request) {
        List<CategoryDTO> categories = categoryService.getCategoriesByTypeForCurrentUser("expense");
        String categoryList = categories.stream()
                .map(c -> c.getName() + " (id: " + c.getId() + ")")
                .collect(Collectors.joining(", "));

        String prompt = """
                Parse this expense description: "%s"
                Available categories: %s
                Today is %s.

                Return ONLY valid JSON with no markdown, no code fences, in this exact format:
                {"name": "expense name", "amount": 500, "date": "YYYY-MM-DD", "suggestedCategoryName": "category name", "icon": "emoji"}
                """.formatted(request.getText(), categoryList, LocalDate.now());

        String response = geminiService.generateText(prompt);
        return parseExpenseResponse(response, categories);
    }

    public AiChatResponseDTO chat(AiChatRequestDTO request) {
        StringBuilder historyText = new StringBuilder();
        if (request.getHistory() != null) {
            for (AiChatRequestDTO.ChatMessageDTO msg : request.getHistory()) {
                historyText.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            }
        }

        String prompt = """
                You are a finance assistant for a Money Manager app.
                User financial data (includes category breakdowns): %s
                Previous conversation:
                %s
                User question: %s

                Answer using the data above. Use ₹ for currency.
                For "where did I spend most", use expenseByCategory.
                For savings advice, compare income vs expense and top spending categories.
                If data is insufficient, say so clearly. Keep answers concise (under 150 words).
                """.formatted(buildFinancialContext(), historyText, request.getMessage());

        String reply = geminiService.generateText(prompt);
        return AiChatResponseDTO.builder().reply(reply).build();
    }

    public SuggestCategoryResponseDTO suggestCategory(SuggestCategoryRequestDTO request) {
        String type = request.getType() != null ? request.getType() : "expense";
        List<CategoryDTO> categories = categoryService.getCategoriesByTypeForCurrentUser(type);
        if (categories.isEmpty()) {
            // fallback: try all categories for user
            categories = categoryService.getCategoriesForCurrentUser();
        }
        if (categories.isEmpty()) {
            throw new RuntimeException("No categories found. Please create categories first from the Category page.");
        }

        String categoryList = categories.stream()
                .map(c -> c.getName() + " (id: " + c.getId() + ", icon: " + c.getIcon() + ")")
                .collect(Collectors.joining(", "));

        String prompt = """
                User is adding a %s with description: "%s"
                Available categories: %s

                Pick the best matching category from the list ONLY.
                Return ONLY valid JSON on one line, no markdown, no code fences:
                {"categoryName":"exact name from list","icon":"emoji","reason":"one short sentence"}
                """.formatted(type, request.getDescription(), categoryList);

        String response = geminiService.generateText(prompt);
        return parseSuggestCategoryResponse(response, categories);
    }

    public MonthlySummaryResponseDTO getMonthlySummary() {
        Map<String, Object> context = buildFinancialContext();
        String prompt = """
                Write a monthly financial report for the user based on this data:
                %s

                Structure the report as:
                ## Monthly Overview
                (total income, total expense, net savings/deficit)

                ## Top Spending Categories
                (list with amounts in ₹)

                ## Income Sources
                (brief summary)

                ## Highlights & Concerns
                (2-3 observations)

                ## Recommendations
                (2-3 tips for next month)

                Use INR (₹). Write in clear, professional but friendly language. ~250-350 words.
                """.formatted(context);

        String summary = geminiService.generateText(prompt);
        return MonthlySummaryResponseDTO.builder()
                .month(YearMonth.now().toString())
                .summary(summary)
                .build();
    }

    public SavingsPlanResponseDTO createSavingsPlan(SavingsPlanRequestDTO request) {
        Map<String, Object> context = buildFinancialContext();
        double target = request.getTargetAmount() != null ? request.getTargetAmount() : 50000;
        int months = request.getMonths() != null ? request.getMonths() : 3;
        String goal = request.getGoalName() != null ? request.getGoalName() : "Savings Goal";

        String prompt = """
                You are a personal finance coach for an Indian user.
                User financial data: %s

                Create a realistic savings plan for: "%s"
                Target: ₹%.0f in %d months.

                Structure:
                ## Goal Snapshot
                (target, monthly amount needed, feasibility based on their income/expense)

                ## Week-by-Week / Month Plan
                (concrete ₹ amounts they should set aside)

                ## Where to Cut
                (specific categories from their data with ₹ savings estimates)

                ## Auto-Save Rules
                (2-3 simple rules they can follow)

                ## Motivation Check
                (one encouraging closing line)

                Use INR (₹). Be specific with numbers from their data. Under 350 words.
                """.formatted(context, goal, target, months);

        return SavingsPlanResponseDTO.builder().plan(geminiService.generateText(prompt)).build();
    }

    public CutCoachResponseDTO getCutCoachAdvice() {
        Map<String, Object> context = buildFinancialContext();
        String prompt = """
                You are a spending-cut coach for an Indian Money Manager user.
                Financial data: %s

                Analyze where they overspend and give ranked cut recommendations.

                Structure:
                ## Top 3 Cut Opportunities
                (category, current spend, suggested cut %%, ₹ saved/month, why)

                ## Quick Wins (this week)
                (3 tiny actions under ₹500 impact each)

                ## Keep These
                (1-2 categories they should NOT cut)

                ## Potential Monthly Savings
                (total ₹ if they follow the plan)

                Use INR (₹). Be blunt but friendly. Under 300 words.
                """.formatted(context);

        return CutCoachResponseDTO.builder().advice(geminiService.generateText(prompt)).build();
    }

    public WhatIfResponseDTO simulateWhatIf(WhatIfRequestDTO request) {
        Map<String, Object> context = buildFinancialContext();
        String scenario = request.getScenario() != null ? request.getScenario() : "cut food by 20%";

        String prompt = """
                You are a financial what-if simulator for an Indian user.
                Current financial data: %s

                Scenario to simulate: "%s"

                Respond with:
                ## Scenario Understood
                (restate in one line)

                ## Projected Impact
                (₹ saved or spent change this month, new estimated savings rate)

                ## New Monthly Picture
                (approx new expense total, new net savings)

                ## Risk / Side Effects
                (1-2 downsides)

                ## Verdict
                (Do it / Do it partially / Skip — with one reason)

                Use INR (₹). Invent reasonable estimates from their category data. Under 280 words.
                """.formatted(context, scenario);

        return WhatIfResponseDTO.builder().analysis(geminiService.generateText(prompt)).build();
    }

    public WeeklyDigestResponseDTO getWeeklyDigest() {
        Map<String, Object> context = buildFinancialContext();
        String prompt = """
                Write a short weekly money digest newsletter for this user.
                Data: %s

                Format like an email newsletter:
                ## This Week in Your Money
                (2-3 sentence opener)

                ## Wins
                (1-2 positives)

                ## Watch Out
                (1-2 concerns)

                ## One Action for Next Week
                (single concrete tip)

                Use INR (₹). Friendly tone. Under 200 words.
                """.formatted(context);

        return WeeklyDigestResponseDTO.builder().digest(geminiService.generateText(prompt)).build();
    }

    private Map<String, Object> buildFinancialContext() {
        Map<String, Object> context = new LinkedHashMap<>(dashboardService.getDashboardData());
        List<ExpenseDTO> monthExpenses = expenseService.getCurrentMonthExpensesForCurrentUser();
        List<IncomeDTO> monthIncomes = incomeService.getCurrentMonthIncomesForCurrentUser();

        context.put("currentMonthExpenses", monthExpenses);
        context.put("currentMonthIncomes", monthIncomes);
        context.put("expenseByCategory", groupExpensesByCategory(monthExpenses));
        context.put("incomeByCategory", groupIncomesByCategory(monthIncomes));
        context.put("month", YearMonth.now().toString());
        return context;
    }

    private Map<String, BigDecimal> groupExpensesByCategory(List<ExpenseDTO> expenses) {
        return expenses.stream().collect(Collectors.groupingBy(
                e -> e.getCategoryName() != null ? e.getCategoryName() : "Other",
                Collectors.reducing(BigDecimal.ZERO, ExpenseDTO::getAmount, BigDecimal::add)
        ));
    }

    private Map<String, BigDecimal> groupIncomesByCategory(List<IncomeDTO> incomes) {
        return incomes.stream().collect(Collectors.groupingBy(
                i -> i.getCategoryName() != null ? i.getCategoryName() : "Other",
                Collectors.reducing(BigDecimal.ZERO, IncomeDTO::getAmount, BigDecimal::add)
        ));
    }

    private SuggestCategoryResponseDTO parseSuggestCategoryResponse(String response, List<CategoryDTO> categories) {
        try {
            String json = extractJson(response);
            JsonNode node = objectMapper.readTree(json);

            String categoryName = node.path("categoryName").asText("");
            String icon = node.path("icon").asText("💰");
            String reason = node.path("reason").asText("Category suggested by AI");

            Long matchedId = categories.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                    .map(CategoryDTO::getId)
                    .findFirst()
                    .orElse(null);

            // Fuzzy: description/name contains match
            if (matchedId == null && categoryName != null && !categoryName.isBlank()) {
                matchedId = categories.stream()
                        .filter(c -> categoryName.toLowerCase().contains(c.getName().toLowerCase())
                                || c.getName().toLowerCase().contains(categoryName.toLowerCase()))
                        .map(CategoryDTO::getId)
                        .findFirst()
                        .orElse(null);
            }
            if (matchedId == null) {
                matchedId = categories.get(0).getId();
            }

            final Long categoryId = matchedId;

            String resolvedName = categories.stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .map(CategoryDTO::getName)
                    .findFirst()
                    .orElse(categoryName);

            String resolvedIcon = categories.stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .map(CategoryDTO::getIcon)
                    .filter(i -> i != null && !i.isBlank())
                    .findFirst()
                    .orElse(icon);

            return SuggestCategoryResponseDTO.builder()
                    .categoryId(categoryId)
                    .categoryName(resolvedName)
                    .icon(resolvedIcon)
                    .reason(reason)
                    .build();
        } catch (Exception e) {
            // Keyword fallback when Gemini returns non-JSON
            String lower = (response == null ? "" : response).toLowerCase();
            CategoryDTO matched = categories.stream()
                    .filter(c -> lower.contains(c.getName().toLowerCase()))
                    .findFirst()
                    .orElse(categories.get(0));

            return SuggestCategoryResponseDTO.builder()
                    .categoryId(matched.getId())
                    .categoryName(matched.getName())
                    .icon(matched.getIcon() != null ? matched.getIcon() : "💰")
                    .reason("Best match from your categories")
                    .build();
        }
    }

    private ParseExpenseResponseDTO parseExpenseResponse(String response, List<CategoryDTO> categories) {
        try {
            String json = extractJson(response);
            JsonNode node = objectMapper.readTree(json);

            String name = node.path("name").asText("");
            BigDecimal amount = new BigDecimal(node.path("amount").asText("0"));
            LocalDate date = LocalDate.parse(node.path("date").asText(LocalDate.now().toString()));
            String suggestedCategory = node.path("suggestedCategoryName").asText("");
            String icon = node.path("icon").asText("💸");

            Long categoryId = categories.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(suggestedCategory))
                    .map(CategoryDTO::getId)
                    .findFirst()
                    .orElse(categories.isEmpty() ? null : categories.get(0).getId());

            return ParseExpenseResponseDTO.builder()
                    .name(name)
                    .amount(amount)
                    .date(date)
                    .categoryId(categoryId)
                    .icon(icon)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse expense from AI response: " + response);
        }
    }

    private String extractJson(String response) {
        String trimmed = response.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
