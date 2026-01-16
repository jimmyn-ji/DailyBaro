package com.dailybaro.content.service;

import com.dailybaro.common.vo.MysteryBoxVO;
import com.dailybaro.common.util.Result;
import java.util.List;

public interface MysteryBoxService {

    /**
     * Draws a mystery box for the user for the current day.
     * @param userId The user's ID.
     * @return The drawn box item. Returns the already drawn box if tried again on the same day.
     */
    Result<MysteryBoxVO> drawBox(Long userId);

    /**
     * Marks a task from a drawn box as completed.
     * @param drawnBoxId The ID of the drawn box record.
     * @param userId The user's ID (for authorization).
     * @return The updated drawn box record.
     */
    Result<MysteryBoxVO> completeTask(Long drawnBoxId, Long userId);

    /**
     * Gets today's mystery box status for the user.
     * @param userId The user's ID.
     * @return Today's mystery box if exists, null otherwise.
     */
    Result<MysteryBoxVO> getTodayStatus(Long userId);

    /**
     * Gets all today's mystery box records for the user.
     * @param userId The user's ID.
     * @return List of today's mystery box records.
     */
    Result<List<MysteryBoxVO>> getTodayRecords(Long userId);

    /**
     * Gets user's current energy value.
     * @param userId The user's ID.
     * @return The user's current energy value.
     */
    Result<Integer> getUserEnergy(Long userId);
} 