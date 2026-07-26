package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.*;
import in.bushansirgur.moneymanager.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/insights")
    public ResponseEntity<AiInsightsResponseDTO> getInsights() {
        return ResponseEntity.ok(aiService.getInsights());
    }

    @PostMapping("/parse-expense")
    public ResponseEntity<ParseExpenseResponseDTO> parseExpense(@RequestBody ParseExpenseRequestDTO request) {
        return ResponseEntity.ok(aiService.parseExpense(request));
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDTO> chat(@RequestBody AiChatRequestDTO request) {
        return ResponseEntity.ok(aiService.chat(request));
    }

    @PostMapping("/suggest-category")
    public ResponseEntity<?> suggestCategory(@RequestBody SuggestCategoryRequestDTO request) {
        try {
            return ResponseEntity.ok(aiService.suggestCategory(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Suggest category failed"));
        }
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponseDTO> getMonthlySummary() {
        return ResponseEntity.ok(aiService.getMonthlySummary());
    }

    @PostMapping("/savings-plan")
    public ResponseEntity<SavingsPlanResponseDTO> savingsPlan(@RequestBody SavingsPlanRequestDTO request) {
        return ResponseEntity.ok(aiService.createSavingsPlan(request));
    }

    @GetMapping("/cut-coach")
    public ResponseEntity<CutCoachResponseDTO> cutCoach() {
        return ResponseEntity.ok(aiService.getCutCoachAdvice());
    }

    @PostMapping("/what-if")
    public ResponseEntity<WhatIfResponseDTO> whatIf(@RequestBody WhatIfRequestDTO request) {
        return ResponseEntity.ok(aiService.simulateWhatIf(request));
    }

    @GetMapping("/weekly-digest")
    public ResponseEntity<WeeklyDigestResponseDTO> weeklyDigest() {
        return ResponseEntity.ok(aiService.getWeeklyDigest());
    }
}
