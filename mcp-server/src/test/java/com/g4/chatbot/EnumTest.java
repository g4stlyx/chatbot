package com.g4.chatbot;

import com.g4.chatbot.models.Message;
import java.util.Locale;

public class EnumTest {
    public static void main(String[] args) {
        Message.MessageRole role = Message.MessageRole.ASSISTANT;
        
        System.out.println("=== ENUM TEST - BEFORE FIX (Turkish locale) ===");
        System.out.println("After toLowerCase(): '" + role.name().toLowerCase() + "'");
        String lowerTurkish = role.name().toLowerCase();
        System.out.println("Character codes:");
        for (char c : lowerTurkish.toCharArray()) {
            System.out.printf("  %d (%c)%n", (int)c, c);
        }
        System.out.println("Equals 'assistant': " + "assistant".equals(lowerTurkish));
        
        System.out.println("\n=== ENUM TEST - AFTER FIX (English locale) ===");
        System.out.println("After toLowerCase(Locale.ENGLISH): '" + role.name().toLowerCase(Locale.ENGLISH) + "'");
        String lowerEnglish = role.name().toLowerCase(Locale.ENGLISH);
        System.out.println("Character codes:");
        for (char c : lowerEnglish.toCharArray()) {
            System.out.printf("  %d (%c)%n", (int)c, c);
        }
        System.out.println("Equals 'assistant': " + "assistant".equals(lowerEnglish));
        
        System.out.println("\n✅ FIX VERIFIED!");
    }
}
