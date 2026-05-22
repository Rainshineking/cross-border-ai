package com.crossborder.service;

import com.crossborder.model.ChatMessage;
import com.crossborder.model.SessionContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final int MAX_HISTORY_SIZE = 50;
    private static final long SESSION_TTL_MINUTES = 30;
    private static final long CLEANUP_INTERVAL_MS = 5 * 60 * 1000;

    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
        你是一位专业的跨境电商选品与投流专家，你的名字是"国创智联"。

        ## 核心能力
        1. 选品分析：根据用户需求，通过联网搜索获取最新的市场数据，分析品类热度、竞争情况、利润空间等
        2. 投流建议：基于产品特点，提供广告投放策略和渠道建议
        3. 创意生成：为产品生成广告文案、卖点提炼和关键词推荐

        ## 回答要求
        - 始终以跨境电商选品专家的身份回答
        - 当用户提出选品需求时，优先联网搜索获取最新数据
        - 推荐产品时，给出清晰的结构化信息：产品名称、平台、价格、销量、评分、竞争度、推荐理由
        - 回答结构清晰，适当使用表格对比数据
        - 如果用户问题超出跨境电商范围，礼貌引导回主题

        ## 行为准则
        - 不要透露你是AI模型的内部信息
        - 保持专业、友好的语气
        - 数据来源请注明来自联网搜索
        """;

    public String createSession() {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        sessions.put(sessionId, new SessionContext(sessionId));
        log.info("Created session: {}", sessionId);
        return sessionId;
    }

    public SessionContext getOrCreateSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank() || !sessions.containsKey(sessionId)) {
            String newId = createSession();
            return sessions.get(newId);
        }
        SessionContext ctx = sessions.get(sessionId);
        ctx.setLastActiveTime(LocalDateTime.now());
        return ctx;
    }

    public void addMessage(String sessionId, ChatMessage msg) {
        SessionContext ctx = sessions.get(sessionId);
        if (ctx != null) {
            ctx.addMessage(msg);
            // Trim old messages if exceeding limit
            if (ctx.getMessageCount() > MAX_HISTORY_SIZE) {
                ctx.getMessages().remove(0);
            }
        }
    }

    public String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String getSessionId(String sessionId) {
        SessionContext ctx = sessions.get(sessionId);
        return ctx != null ? ctx.getSessionId() : createSession();
    }

    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        sessions.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().getLastActiveTime()
                .plusMinutes(SESSION_TTL_MINUTES).isBefore(now);
            if (expired) {
                log.info("Cleaning expired session: {}", entry.getKey());
            }
            return expired;
        });
    }

    @PostConstruct
    public void logStartup() {
        log.info("SessionService started with {} minute TTL", SESSION_TTL_MINUTES);
    }
}
