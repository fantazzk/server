package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.application.RoomRealtimeEventPublisher;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class RoomRealtimeEventPublisherConfiguration {
    @Bean
    @ConditionalOnMissingBean(RoomRealtimeEventPublisher.class)
    @ConditionalOnExpression(
        "T(Boolean).parseBoolean('${fantazzk.supabase.realtime.enabled:false}') and " +
        "T(org.springframework.util.StringUtils).hasText('${fantazzk.supabase.url:}') and " +
        "T(org.springframework.util.StringUtils).hasText('${fantazzk.supabase.service-role-key:}')"
    )
    public RoomRealtimeEventPublisher supabaseRoomSnapshotPublisher(
        RestClient.Builder restClientBuilder,
        Clock clock,
        @Value("${fantazzk.supabase.url}") String supabaseUrl,
        @Value("${fantazzk.supabase.service-role-key}") String serviceRoleKey,
        @Value("${fantazzk.supabase.realtime.topic-prefix:room}") String topicPrefix
    ) {
        return new SupabaseRoomRealtimePublisher(restClientBuilder, clock, supabaseUrl, serviceRoleKey, topicPrefix);
    }

    @Bean
    @ConditionalOnMissingBean(RoomRealtimeEventPublisher.class)
    public RoomRealtimeEventPublisher noopRoomRealtimeEventPublisher() {
        return new NoopRoomRealtimeEventPublisher();
    }
}
