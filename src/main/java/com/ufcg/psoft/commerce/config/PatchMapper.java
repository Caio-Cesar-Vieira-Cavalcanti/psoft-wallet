package com.ufcg.psoft.commerce.config;

import org.modelmapper.ModelMapper;

public final class PatchMapper {
    private static final ModelMapper PATCH_MAPPER;

    private PatchMapper() {
        throw new IllegalStateException("Utility class");
    }

    static {
        PATCH_MAPPER = new ModelMapper();
        PATCH_MAPPER.getConfiguration()
                .setPropertyCondition(ctx -> ctx.getSource() != null);
    }

    public static <S, D> void mapNonNull(S source, D destination) {
        PATCH_MAPPER.map(source, destination);
    }
}
