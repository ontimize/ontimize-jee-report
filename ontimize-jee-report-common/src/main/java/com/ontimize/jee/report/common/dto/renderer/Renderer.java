package com.ontimize.jee.report.common.dto.renderer;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BooleanRendererDto.class, name = "boolean"),
        @JsonSubTypes.Type(value = CurrencyRendererDto.class, name = "currency"),
        @JsonSubTypes.Type(value = DateRendererDto.class, name = "date"),
        @JsonSubTypes.Type(value = IntegerRendererDto.class, name = "integer"),
        @JsonSubTypes.Type(value = RealRendererDto.class, name = "real"),
        @JsonSubTypes.Type(value = ServiceRendererDto.class, name = "service"),
//TODO
//        @JsonSubTypes.Type(value = IntegerRendererDto.class, name = "percentage"),
//        @JsonSubTypes.Type(value = IntegerRendererDto.class, name = "image"),
//        @JsonSubTypes.Type(value = ServiceRendererDto.class, name = "time")
})
public interface Renderer {

    String getType();
}
