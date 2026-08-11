package com.markokosic.minicrm.common.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PageResponseDTO<T> {
	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;

	public PageResponseDTO(List<T> content, int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
		this.content = content;
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.first = first;
		this.last = last;
	}

	public static <T> PageResponseDTO<T> from(Page<T> page) {
		return new PageResponseDTO<>(
				page.getContent(),
				page.getNumber() + 1,
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast()
		);
	}
}
