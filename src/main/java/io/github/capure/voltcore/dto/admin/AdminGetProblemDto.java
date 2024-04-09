package io.github.capure.voltcore.dto.admin;

import io.github.capure.voltcore.dto.GetProblemDto;
import io.github.capure.voltcore.model.Problem;
import io.github.capure.voltcore.util.Base64Helper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminGetProblemDto extends GetProblemDto {
    private List<GetTestCaseDto> testCases;

    public AdminGetProblemDto(Problem p) {
        super(p);
        testCases = p.getTestCases().stream().map(GetTestCaseDto::new).toList();
    }

    public AdminGetProblemDto decode() {
        this.setDescription(Base64Helper.fromBase64(this.getDescription()));
        this.setTemplate(Base64Helper.fromBase64(this.getTemplate()));
        this.setTestCases(this.getTestCases().stream().peek(t -> {
            t.setInput(Base64Helper.fromBase64(t.getInput()));
            t.setOutput(Base64Helper.fromBase64(t.getOutput()));
        }).toList());
        return this;
    }
}
