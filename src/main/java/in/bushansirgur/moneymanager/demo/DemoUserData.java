package in.bushansirgur.moneymanager.demo;

import in.bushansirgur.moneymanager.dto.CategoryDTO;
import in.bushansirgur.moneymanager.dto.ExpenseDTO;
import in.bushansirgur.moneymanager.dto.IncomeDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Hardcoded demo user data — NOT stored in DB for transactions/categories.
 * Login account still exists in DB for auth only.
 */
public final class DemoUserData {

    public static final String EMAIL = "abcd@gmail.com";
    public static final String PASSWORD = "abcd";
    public static final String FULL_NAME = "Demo User";
    public static final Long PROFILE_ID = 9001L;

    private DemoUserData() {}

    public static boolean isDemoEmail(String email) {
        return email != null && EMAIL.equalsIgnoreCase(email.trim());
    }

    public static List<CategoryDTO> categories() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                cat(101L, "Food & Dining", "🍔", "expense", now),
                cat(102L, "Transport", "🚗", "expense", now),
                cat(103L, "Shopping", "🛍️", "expense", now),
                cat(104L, "Utilities", "💡", "expense", now),
                cat(105L, "Entertainment", "🎬", "expense", now),
                cat(106L, "Health", "🏥", "expense", now),
                cat(107L, "Rent", "🏠", "expense", now),
                cat(108L, "Education", "📚", "expense", now),
                cat(201L, "Salary", "💼", "income", now),
                cat(202L, "Freelance", "💻", "income", now),
                cat(203L, "Investments", "📈", "income", now),
                cat(204L, "Bonus", "🎁", "income", now)
        );
    }

    public static List<CategoryDTO> categoriesByType(String type) {
        return categories().stream()
                .filter(c -> type == null || type.equalsIgnoreCase(c.getType()))
                .toList();
    }

    public static List<IncomeDTO> incomes() {
        LocalDate today = LocalDate.now();
        int y = today.getYear();
        int m = today.getMonthValue();
        LocalDateTime now = LocalDateTime.now();

        return List.of(
                income(1001L, "Monthly Salary", "💼", "Salary", 201L, "85000", LocalDate.of(y, m, 1), now),
                income(1002L, "Freelance UI Project", "💻", "Freelance", 202L, "18000", LocalDate.of(y, m, Math.min(5, today.lengthOfMonth())), now),
                income(1003L, "Mutual Fund Returns", "📈", "Investments", 203L, "4200", LocalDate.of(y, m, Math.min(8, today.lengthOfMonth())), now),
                income(1004L, "Performance Bonus", "🎁", "Bonus", 204L, "12000", LocalDate.of(y, m, Math.min(12, today.lengthOfMonth())), now),
                income(1005L, "Weekend Consulting", "💻", "Freelance", 202L, "7500", LocalDate.of(y, m, Math.min(18, today.lengthOfMonth())), now),
                income(1006L, "Dividend Payout", "📈", "Investments", 203L, "2100", LocalDate.of(y, m, Math.min(22, today.lengthOfMonth())), now)
        );
    }

    public static List<ExpenseDTO> expenses() {
        LocalDate today = LocalDate.now();
        int y = today.getYear();
        int m = today.getMonthValue();
        LocalDateTime now = LocalDateTime.now();

        return List.of(
                expense(2001L, "Apartment Rent", "🏠", "Rent", 107L, "22000", LocalDate.of(y, m, 1), now),
                expense(2002L, "Groceries - BigBasket", "🍔", "Food & Dining", 101L, "4500", LocalDate.of(y, m, Math.min(3, today.lengthOfMonth())), now),
                expense(2003L, "Uber to Office", "🚗", "Transport", 102L, "680", LocalDate.of(y, m, Math.min(4, today.lengthOfMonth())), now),
                expense(2004L, "Electricity Bill", "💡", "Utilities", 104L, "1850", LocalDate.of(y, m, Math.min(6, today.lengthOfMonth())), now),
                expense(2005L, "Netflix + Spotify", "🎬", "Entertainment", 105L, "849", LocalDate.of(y, m, Math.min(7, today.lengthOfMonth())), now),
                expense(2006L, "Amazon Shopping", "🛍️", "Shopping", 103L, "3200", LocalDate.of(y, m, Math.min(9, today.lengthOfMonth())), now),
                expense(2007L, "Swiggy Weekend", "🍔", "Food & Dining", 101L, "1250", LocalDate.of(y, m, Math.min(11, today.lengthOfMonth())), now),
                expense(2008L, "Pharmacy Medicines", "🏥", "Health", 106L, "980", LocalDate.of(y, m, Math.min(13, today.lengthOfMonth())), now),
                expense(2009L, "Petrol Fill", "🚗", "Transport", 102L, "2500", LocalDate.of(y, m, Math.min(15, today.lengthOfMonth())), now),
                expense(2010L, "Internet Bill", "💡", "Utilities", 104L, "999", LocalDate.of(y, m, Math.min(16, today.lengthOfMonth())), now),
                expense(2011L, "Movie Night", "🎬", "Entertainment", 105L, "700", LocalDate.of(y, m, Math.min(19, today.lengthOfMonth())), now),
                expense(2012L, "Online Course", "📚", "Education", 108L, "2499", LocalDate.of(y, m, Math.min(20, today.lengthOfMonth())), now),
                expense(2013L, "Cafe Meetup", "🍔", "Food & Dining", 101L, "640", LocalDate.of(y, m, Math.min(21, today.lengthOfMonth())), now),
                expense(2014L, "New Headphones", "🛍️", "Shopping", 103L, "4500", LocalDate.of(y, m, Math.min(23, today.lengthOfMonth())), now)
        );
    }

    public static BigDecimal totalIncome() {
        return incomes().stream().map(IncomeDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal totalExpense() {
        return expenses().stream().map(ExpenseDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static CategoryDTO cat(Long id, String name, String icon, String type, LocalDateTime now) {
        return CategoryDTO.builder()
                .id(id)
                .profileId(PROFILE_ID)
                .name(name)
                .icon(icon)
                .type(type)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static IncomeDTO income(Long id, String name, String icon, String catName, Long catId,
                                    String amount, LocalDate date, LocalDateTime now) {
        return IncomeDTO.builder()
                .id(id)
                .name(name)
                .icon(icon)
                .categoryName(catName)
                .categoryId(catId)
                .amount(new BigDecimal(amount))
                .date(date)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private static ExpenseDTO expense(Long id, String name, String icon, String catName, Long catId,
                                      String amount, LocalDate date, LocalDateTime now) {
        return ExpenseDTO.builder()
                .id(id)
                .name(name)
                .icon(icon)
                .categoryName(catName)
                .categoryId(catId)
                .amount(new BigDecimal(amount))
                .date(date)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
