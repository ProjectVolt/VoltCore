package io.github.capure.voltcore.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class VoltSettings {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Nonnull @NotNull @Size(min=2, max=20)
    private String deploymentName;
    @Nonnull @NotNull @Size(min=3)
    private String deploymentBaseUrl;
    @Nonnull @NotNull
    private Boolean allowRegister;

    public static VoltSettings getDefault() {
        return new VoltSettings(null, "Volt", "localhost", true);
    }

    @Override
    public String toString() {
        return String.format("deploymentName: %s deploymentBaseUrl: %s allowRegister: %s", deploymentName, deploymentBaseUrl, allowRegister);
    }
}
