package com.markokosic.minicrm.modules.revenue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateDailyRevenueBulkRequestDTO(
		@Valid
		@NotEmpty
		 List<CreateDailyRevenueRequestDTO> dailyRevenueRecords

) {
}
