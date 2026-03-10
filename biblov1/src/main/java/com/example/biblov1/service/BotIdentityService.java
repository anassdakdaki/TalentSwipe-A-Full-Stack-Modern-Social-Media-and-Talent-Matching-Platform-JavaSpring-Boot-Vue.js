package com.example.biblov1.service;

import com.example.biblov1.model.User;
import com.example.biblov1.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BotIdentityService {

    private final BotPopulationService botPopulationService;
    private final UserRepository userRepository;

    public BotIdentityService(@Lazy BotPopulationService botPopulationService, UserRepository userRepository) {
        this.botPopulationService = botPopulationService;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BotPersona> getActiveBotPersonas() {
        List<BotPopulationService.BotDefinitionSnapshot> definitions = botPopulationService.getActiveBotDefinitionsSnapshot();
        if (definitions.isEmpty()) {
            return List.of();
        }

        List<BotPersona> personas = new ArrayList<>();
        for (BotPopulationService.BotDefinitionSnapshot definition : definitions) {
            User botUser = userRepository.findByEmail(definition.email()).orElse(null);
            if (botUser == null || botUser.getId() == null) {
                continue;
            }

            List<String> interests = definition.interests() == null ? List.of() : new ArrayList<>(definition.interests());
            List<String> welcomeMessages = buildWelcomeVariants(definition.welcomeMessage(), interests);
            personas.add(new BotPersona(
                    botUser.getId(),
                    definition.name(),
                    definition.email(),
                    interests,
                    welcomeMessages
            ));
        }
        return personas;
    }

    @Transactional(readOnly = true)
    public Set<Long> getActiveBotUserIdSet() {
        return getActiveBotPersonas().stream()
                .map(BotPersona::userId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional(readOnly = true)
    public List<Long> getActiveBotUserIds() {
        return new ArrayList<>(getActiveBotUserIdSet());
    }

    @Transactional(readOnly = true)
    public boolean isBotUserId(Long userId) {
        return userId != null && getActiveBotUserIdSet().contains(userId);
    }

    @Transactional(readOnly = true)
    public boolean isBotEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        return getActiveBotPersonas().stream()
                .anyMatch(persona -> persona.email().toLowerCase(Locale.ROOT).equals(normalized));
    }

    @Transactional(readOnly = true)
    public Optional<BotPersona> findBotPersonaByUserId(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return getActiveBotPersonas().stream()
                .filter(persona -> userId.equals(persona.userId()))
                .findFirst();
    }

    @Transactional(readOnly = true)
    public List<Long> filterBotUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        Set<Long> botIds = getActiveBotUserIdSet();
        return userIds.stream()
                .filter(Objects::nonNull)
                .filter(botIds::contains)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String pickWelcomeMessage(Long botUserId, Long humanUserId) {
        BotPersona persona = findBotPersonaByUserId(botUserId).orElse(null);
        if (persona == null) {
            return null;
        }
        List<String> variants = persona.welcomeMessages();
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        int index = Math.floorMod(Objects.hash(botUserId, humanUserId), variants.size());
        return variants.get(index);
    }

    @Transactional(readOnly = true)
    public Map<Long, BotPersona> getBotPersonaMapByUserId() {
        Map<Long, BotPersona> byUserId = new LinkedHashMap<>();
        for (BotPersona persona : getActiveBotPersonas()) {
            byUserId.put(persona.userId(), persona);
        }
        return byUserId;
    }

    private List<String> buildWelcomeVariants(String baseMessage, List<String> interests) {
        LinkedHashSet<String> variants = new LinkedHashSet<>();
        if (baseMessage != null && !baseMessage.isBlank()) {
            variants.add(baseMessage.trim());
        }

        String primaryInterest = (interests == null || interests.isEmpty()) ? "your current goals" : interests.get(0);
        variants.add("Great match. If you want, we can start with " + primaryInterest + " and make a simple plan.");
        variants.add("Nice to connect here. Tell me one thing you are building this week and I will share practical feedback.");
        variants.add("Happy we matched. I can help with a short checklist if you want to move faster this week.");

        return new ArrayList<>(variants);
    }

    public record BotPersona(
            Long userId,
            String name,
            String email,
            List<String> interests,
            List<String> welcomeMessages
    ) {
    }
}
