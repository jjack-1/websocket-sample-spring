package org.example.websocketsamplespring.service;

import org.example.websocketsamplespring.model.Participant;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StreamService {

    // 스트림 키를 key로, 해당 채널의 참가자 목록을 value로 저장 (DB 역할 대행)
    private final Map<String, List<Participant>> channelParticipants = new ConcurrentHashMap<>();

    // 스트림의 소유자를 저장하는 맵 (DB 역할 대행)
    private final Map<String, String> streamOwners = new ConcurrentHashMap<>();

    // --- 소유권 관련 로직 ---

    /**
     * 특정 사용자가 해당 스트림의 소유자인지 확인하는 메서드.
     *
     * @param streamKey 확인할 스트림 키
     * @param userId    확인할 사용자 ID
     * @return 소유자 여부
     */
    public boolean isStreamOwner(String streamKey, String userId) {
        // 실제로는 DB를 조회해야 합니다.
        // 여기서는 시뮬레이션을 위해 "streamer-" + streamKey 가 소유자라고 가정합니다.
        // (예: a1b2c3d4 스트림의 주인은 streamer-a1b2c3d4)
        String ownerId = "streamer-" + streamKey; // 임시 소유자 ID
        streamOwners.putIfAbsent(streamKey, ownerId); // 소유자 정보가 없으면 임시로 저장
        return ownerId.equals(userId);
    }

    /**
     * 구독 목적지 주소에서 스트림 키를 추출하는 헬퍼 메서드
     *
     * @param destination /sub/{streamKey}/... 형식의 주소
     * @return 추출된 streamKey
     */
    public String extractStreamKeyFromDestination(String destination) {
        if (destination == null) return null;
        // 정규표현식을 사용하여 /sub/스트림키/어딘가 에서 '스트림키' 부분 추출
        Pattern pattern = Pattern.compile("/sub/([^/]+)/.*");
        Matcher matcher = pattern.matcher(destination);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    // --- 참가자 관리 로직 (생략 없는 전체 버전) ---

    /**
     * 채널에 참가자를 추가하는 메서드
     */
    public void addParticipant(String streamKey, String userId, String nickname) {
        // computeIfAbsent: key가 존재하지 않으면 새로운 ArrayList를 생성하고, 존재하면 기존 리스트를 반환
        List<Participant> participants = channelParticipants.computeIfAbsent(streamKey, k -> new ArrayList<>());

        // 중복 입장을 방지하기 위해 기존 참가자 목록에서 제거 후 다시 추가
        participants.removeIf(p -> p.getUserId().equals(userId));
        participants.add(new Participant(userId, nickname));

        System.out.println("참가자 추가: " + nickname + " -> 채널 " + streamKey);
    }

    /**
     * 채널에서 참가자를 제거하는 메서드
     */
    public void removeParticipant(String streamKey, String userId) {
        List<Participant> participants = channelParticipants.get(streamKey);
        if (participants != null) {
            boolean removed = participants.removeIf(p -> p.getUserId().equals(userId));
            if (removed) {
                System.out.println("참가자 제거: " + userId + " -> 채널 " + streamKey);
            }
            // 참가자가 한 명도 없으면 맵에서 채널 정보를 제거하여 메모리 누수 방지
            if (participants.isEmpty()) {
                channelParticipants.remove(streamKey);
                System.out.println("채널 비어있음, 제거: " + streamKey);
            }
        }
    }

    /**
     * 특정 채널의 모든 참가자 목록을 반환하는 메서드
     */
    public List<Participant> getParticipants(String streamKey) {
        // 채널이 존재하지 않으면 빈 리스트를 반환하여 NullPointerException 방지
        return channelParticipants.getOrDefault(streamKey, new ArrayList<>());
    }
}
