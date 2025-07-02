package com.codestorykh.alpha.identity.dto;

import com.codestorykh.alpha.identity.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {

    private List<User> users;
    private PaginationInfo pagination;
    private SearchInfo searchInfo;
    private SummaryInfo summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int pageSize;
        private boolean hasNext;
        private boolean hasPrevious;
        private boolean isFirst;
        private boolean isLast;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchInfo {
        private UserSearchCriteria criteria;
        private String searchMode;
        private String sortBy;
        private String sortDirection;
        private long searchTimeMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryInfo {
        private long totalUsers;
        private long activeUsers;
        private long lockedUsers;
        private long disabledUsers;
        private long unverifiedUsers;
    }

    public static UserSearchResponse fromPage(Page<User> page, UserSearchCriteria criteria, long searchTimeMs) {
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();

        SearchInfo searchInfo = SearchInfo.builder()
                .criteria(criteria)
                .searchMode(criteria.getSearchMode().name())
                .sortBy(criteria.getSortBy())
                .sortDirection(criteria.getSortDirection())
                .searchTimeMs(searchTimeMs)
                .build();

        return UserSearchResponse.builder()
                .users(page.getContent())
                .pagination(pagination)
                .searchInfo(searchInfo)
                .build();
    }
} 