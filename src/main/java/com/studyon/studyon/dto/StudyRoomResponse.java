package com.studyon.studyon.dto;

import com.studyon.studyon.domain.RoomType;
import com.studyon.studyon.domain.StudyRoom;

import java.time.LocalTime;

public record StudyRoomResponse(
        Long id,
        String name,
        RoomType roomType,
        int minCapacity,
        int maxCapacity,
        LocalTime openTime,
        LocalTime closeTime
) {

    public static StudyRoomResponse from(StudyRoom studyRoom) {
        return new StudyRoomResponse(
                studyRoom.getId(),
                studyRoom.getName(),
                studyRoom.getRoomType(),
                studyRoom.getMinCapacity(),
                studyRoom.getMaxCapacity(),
                studyRoom.getOpenTime(),
                studyRoom.getCloseTime()
        );
    }
}
