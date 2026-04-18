package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.room.application.room.ClearDraftPosition;
import com.naminhyeok.fantazzk.room.application.room.SelectDraftPosition;
import org.springframework.stereotype.Service;

public interface RoomDraftApi {
    RoomView selectDraftPosition(String code, String actionToken, int draftPosition);

    RoomView clearDraftPosition(String code, String actionToken);
}

@Service
class ProvideRoomDraftApi implements RoomDraftApi {
    private final SelectDraftPosition selectDraftPosition;
    private final ClearDraftPosition clearDraftPosition;

    ProvideRoomDraftApi(SelectDraftPosition selectDraftPosition, ClearDraftPosition clearDraftPosition) {
        this.selectDraftPosition = selectDraftPosition;
        this.clearDraftPosition = clearDraftPosition;
    }

    @Override
    public RoomView selectDraftPosition(String code, String actionToken, int draftPosition) {
        return RoomView.from(selectDraftPosition.select(code, actionToken, draftPosition));
    }

    @Override
    public RoomView clearDraftPosition(String code, String actionToken) {
        return RoomView.from(clearDraftPosition.clear(code, actionToken));
    }
}
