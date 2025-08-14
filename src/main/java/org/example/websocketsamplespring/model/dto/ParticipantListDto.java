package org.example.websocketsamplespring.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.websocketsamplespring.model.Participant;

import java.util.List;

@Data
@AllArgsConstructor
public class ParticipantListDto {
    private List<Participant> participants;
}
