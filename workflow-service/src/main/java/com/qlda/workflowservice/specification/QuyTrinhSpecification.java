package com.qlda.workflowservice.specification;

import com.qlda.workflowservice.entity.QuyTrinh;
import org.springframework.data.jpa.domain.Specification;

public final class QuyTrinhSpecification {
    private QuyTrinhSpecification() {
    }

    public static Specification<QuyTrinh> filter(String keyword, Integer loaiVanBanId, Boolean suDung) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates = cb.and(
                        predicates,
                        cb.or(
                                cb.like(cb.lower(root.get("maQuyTrinh")), like),
                                cb.like(cb.lower(root.get("tenQuyTrinh")), like),
                                cb.like(cb.lower(root.get("moTa")), like)
                        )
                );
            }
            if (loaiVanBanId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("loaiVanBanId"), loaiVanBanId));
            }
            if (suDung != null) {
                predicates = cb.and(predicates, cb.equal(root.get("suDung"), suDung));
            }
            return predicates;
        };
    }
}
