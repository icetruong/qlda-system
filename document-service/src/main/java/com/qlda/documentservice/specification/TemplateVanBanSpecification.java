package com.qlda.documentservice.specification;

import com.qlda.documentservice.entity.TemplateVanBan;
import org.springframework.data.jpa.domain.Specification;

public final class TemplateVanBanSpecification {
    private TemplateVanBanSpecification() {
    }

    public static Specification<TemplateVanBan> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String likeValue = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("maTemplate")), likeValue),
                cb.like(cb.lower(root.get("tenTemplate")), likeValue)
            );
        };
    }

    public static Specification<TemplateVanBan> loaiVanBanId(Integer loaiVanBanId) {
        return (root, query, cb) -> loaiVanBanId == null
            ? cb.conjunction()
            : cb.equal(root.get("loaiVanBan").get("id"), loaiVanBanId);
    }

    public static Specification<TemplateVanBan> suDung(Boolean suDung) {
        return (root, query, cb) -> suDung == null ? cb.conjunction() : cb.equal(root.get("suDung"), suDung);
    }
}

