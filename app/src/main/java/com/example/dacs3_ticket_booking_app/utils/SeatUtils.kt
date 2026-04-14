 package com.example.dacs3_ticket_booking_app.utils

object SeatUtils {
    /**
     * Convert position "row_col" to readable name, e.g. "0_1" -> "A2"
     */
    fun positionToName(position: String): String {
        val parts = position.split("_")
        if (parts.size != 2) return position
        val row = parts[0].toIntOrNull() ?: return position
        val col = parts[1].toIntOrNull() ?: return position
        val rowChar = ('A' + row)
        val colNumber = col + 1
        return "$rowChar$colNumber"
    }

    /**
     * Convert a list of positions to display string, e.g. ["0_0","0_1"] -> "A1, A2"
     */
    fun positionsToDisplay(positions: List<String>): String {
        return positions.map { positionToName(it) }.joinToString(", ")
    }

     /**
     * Convert a single position to display string, e.g. "0_1" -> "A2"
     */
    fun positionToDisplay(position: String): String {
        return positionToName(position)
    }

    /**
     * Parse seat layout string into displayable grid
     * Input: "11111111" hoặc "11110111" (8 ký tự: '1'=ghế, '0'=trống)
     * Output: list of SeatCell (kể cả trống để maintain grid layout)
     * Mặc định: 8 cột cố định
     * Hiển thị: A1, A2, A3, A4, A5, A6, A7, A8
     */
    fun parseSeatLayoutToDisplay(seatLayoutRow: String, rowIdx: Int): List<SeatCell> {
        val cells = mutableListOf<SeatCell>()
        
        seatLayoutRow.forEachIndexed { colIdx, char ->
            val pos = "${rowIdx}_${colIdx}"
            if (char == '1') {
                // Ghế ngồi
                val seatName = "${('A' + rowIdx).toString()}${colIdx + 1}"  // A1, A2, ... A8
                cells.add(SeatCell(position = pos, name = seatName, isAisle = false))
            } else {
                // Vị trí trống (xóa ghế)
                cells.add(SeatCell(position = pos, name = "", isAisle = true))
            }
        }
        return cells
    }

    /**
     * Build complete seat grid from room layout
     * Không cần xử lý lối đi nữa
     */
    fun buildSeatGridFromRoom(seatLayout: List<String>): List<SeatCell> {
        val allCells = mutableListOf<SeatCell>()
        seatLayout.forEachIndexed { rowIdx, rowStr ->
            allCells.addAll(parseSeatLayoutToDisplay(rowStr, rowIdx))
        }
        return allCells
    }


    /**
     * Build a flat list of Seat UI items from a Room's seatLayout matrix
     * Each item: position "row_col", isAisle = true if char == '0'
     */
    data class SeatCell(
        val position: String,   // "row_col"
        val name: String,       // "A1"
        val isAisle: Boolean    // if true, render as empty space
    )

    fun buildSeatGrid(seatLayout: List<String>): List<SeatCell> {
        val cells = mutableListOf<SeatCell>()
        seatLayout.forEachIndexed { rowIdx, rowStr ->
            rowStr.forEachIndexed { colIdx, char ->
                val pos = "${rowIdx}_${colIdx}"
                val isAisle = char == '0'
                val name = if (isAisle) "" else positionToName(pos)
                cells.add(SeatCell(position = pos, name = name, isAisle = isAisle))
            }
        }
        return cells
    }

    // ✅ Hàm label cho priceTier (dùng cho hiển thị)
    fun priceTierLabel(tier: String): String = when (tier) {
        "morning" -> "Sáng"
        "afternoon" -> "Chiều"
        "evening" -> "Tối"
        else -> tier
    }
}
