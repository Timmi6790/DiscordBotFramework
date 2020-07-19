package de.timmi6790.external_modules.mineplexstats.statsapi.models.errors;


import de.timmi6790.external_modules.mineplexstats.statsapi.models.ResponseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class ErrorModel extends ResponseModel {
    private final int errorCode;
    private final String errorMessage;
}
