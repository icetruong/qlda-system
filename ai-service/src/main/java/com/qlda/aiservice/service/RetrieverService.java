package com.qlda.aiservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RetrieverService {
    private final JdbcTemplate jdbcTemplate;

    public List<String> searchByKeyword(String message) {
        String keyword = extractKeyword(message);
        String sql = """
            SELECT noi_dung
            FROM ai_document_chunk
            WHERE noi_dung ILIKE ?
            LIMIT 5
        """;

        return jdbcTemplate.queryForList(
                sql,
                String.class,
                "%" + keyword + "%"
        );
    }

    public List<String> searchUserGuide(String message) {
        String keyword = detectModuleKeyword(message);

        String sql = """
            SELECT noi_dung
            FROM ai_document_chunk
            WHERE metadata ->> 'type' = 'huong_dan'
              AND (
                    LOWER(noi_dung) LIKE LOWER(?)
                    OR LOWER(metadata ->> 'module') LIKE LOWER(?)
                    OR LOWER(metadata ->> 'title') LIKE LOWER(?)
                    OR LOWER(metadata ->> 'role') LIKE LOWER(?)
              )
            ORDER BY chunk_index ASC
            LIMIT 5
        """;

        String param = "%" + keyword + "%";

        return jdbcTemplate.queryForList(
                sql,
                String.class,
                param,
                param,
                param,
                param
        );
    }

    private String detectModuleKeyword(String message) {
        String raw = message.toLowerCase(Locale.ROOT);
        String text = removeVietnameseAccent(raw);

        if (text.contains("dang nhap") || text.contains("login")) {
            return "dang_nhap";
        }

        if (text.contains("dang xuat") || text.contains("logout")) {
            return "dang_xuat";
        }

        if (text.contains("tai van ban")
                || text.contains("upload")
                || text.contains("them van ban")
                || text.contains("nop van ban")) {
            return "tai_van_ban";
        }

        if (text.contains("van ban den") || text.contains("tiep nhan")) {
            return "van_ban_den";
        }

        if (text.contains("ocr") || text.contains("scan") || text.contains("so hoa")) {
            return "ocr";
        }

        if (text.contains("chuyen xu ly")
                || text.contains("chuyen van ban")
                || text.contains("phan cong")) {
            return "chuyen_xu_ly";
        }

        if (text.contains("cong viec cua toi")
                || text.contains("viec duoc giao")
                || text.contains("duoc giao")) {
            return "cong_viec_cua_toi";
        }

        if (text.contains("xu ly cong viec")
                || text.contains("xu ly van ban")
                || text.contains("hoan thanh cong viec")) {
            return "xu_ly_cong_viec";
        }

        if (text.contains("cap nhat tien do") || text.contains("tien do")) {
            return "theo_doi_tien_do";
        }

        if (text.contains("van ban di")) {
            return "van_ban_di";
        }

        if (text.contains("word online")
                || text.contains("soan thao")
                || text.contains("chinh sua truc tuyen")) {
            return "word_online";
        }

        if (text.contains("mau van ban") || text.contains("template")) {
            return "template";
        }

        if (text.contains("trinh duyet") || text.contains("gui duyet")) {
            return "trinh_duyet";
        }

        if (text.contains("phe duyet") || text.contains("duyet van ban")) {
            return "phe_duyet";
        }

        if (text.contains("tu choi") || text.contains("yeu cau chinh sua")) {
            return "tu_choi_van_ban";
        }

        if (text.contains("ky so")) {
            return "ky_so";
        }

        if (text.contains("phat hanh")) {
            return "phat_hanh";
        }

        if (text.contains("tim kiem")
                || text.contains("tra cuu")
                || text.contains("tim van ban")) {
            return "tim_kiem";
        }

        if (text.contains("thong bao")) {
            return "thong_bao";
        }

        if (text.contains("nhac viec")) {
            return "nhac_viec";
        }

        if (text.contains("ho so cong viec")) {
            return "ho_so_cong_viec";
        }

        if (text.contains("phien ban")) {
            return "phien_ban";
        }

        if (text.contains("tom tat")) {
            return "ai_tom_tat";
        }

        if (text.contains("phan loai")) {
            return "ai_phan_loai";
        }

        if (text.contains("trich xuat")) {
            return "ai_trich_xuat";
        }

        if (text.contains("goi y")) {
            return "ai_goi_y";
        }

        if (text.contains("chatbot") || text.contains("hoi")) {
            return "chatbot";
        }

        if (text.contains("sharepoint")) {
            return "sharepoint";
        }

        if (text.contains("onedrive")) {
            return "onedrive_word_online";
        }

        if (text.contains("outlook") || text.contains("email")) {
            return "outlook";
        }

        if (text.contains("teams")) {
            return "teams";
        }

        if (text.contains("dashboard") || text.contains("bao cao")) {
            return "dashboard";
        }

        if (text.contains("nguoi dung") || text.contains("tai khoan")) {
            return "nguoi_dung";
        }

        if (text.contains("phan quyen")) {
            return "phan_quyen";
        }

        if (text.contains("nhat ky") || text.contains("audit")) {
            return "audit_log";
        }

        if (text.contains("sao luu")) {
            return "sao_luu";
        }

        if (text.contains("phuc hoi")) {
            return "phuc_hoi";
        }

        if (text.contains("bao mat")) {
            return "bao_mat";
        }

        return raw;
    }

    private String removeVietnameseAccent(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replace("Đ", "D");
    }

    private String extractKeyword(String message) {
        message = message.toLowerCase();

        if (message.contains("đào tạo")) return "đào tạo";
        if (message.contains("office")) return "office";
        if (message.contains("văn bản")) return "văn bản";

        return message;
    }
}
