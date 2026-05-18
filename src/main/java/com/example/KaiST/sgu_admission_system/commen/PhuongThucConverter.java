package com.example.KaiST.sgu_admission_system.commen;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PhuongThucConverter implements AttributeConverter<PhuongThuc, String> {
    @Override
    public String convertToDatabaseColumn(PhuongThuc attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PhuongThuc convertToEntityAttribute(String dbData) {
        return PhuongThuc.fromCode(dbData);
    }
}
