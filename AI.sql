CREATE TABLE KetQuaAI (
    ID BIGSERIAL PRIMARY KEY,
    VanBanID BIGINT,
    NguoiYeuCauID BIGINT,
    LoaiXuLyAI VARCHAR(100) NOT NULL,
    NoiDungDauVao TEXT,
    KetQuaTraVe TEXT,
    DoTinCay DECIMAL(5,2),
    ModelSuDung VARCHAR(100),
    ThoiGianXuLy TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    GhiChu VARCHAR(500)
);

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai_document_chunk (
    id BIGSERIAL PRIMARY KEY,
    van_ban_id BIGINT NOT NULL,
    tep_dinh_kem_id BIGINT,
    chunk_index INT,
    noi_dung TEXT NOT NULL,
    embedding VECTOR(768),
    metadata JSONB,
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_document_chunk_embedding
ON ai_document_chunk
USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);