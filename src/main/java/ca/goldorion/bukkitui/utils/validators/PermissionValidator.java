package ca.goldorion.bukkitui.utils.validators;

import net.mcreator.ui.init.L10N;
import net.mcreator.ui.validation.ValidationResult;
import net.mcreator.ui.validation.Validator;

import javax.swing.*;
import java.util.regex.Pattern;

public class PermissionValidator implements Validator {

    private final JTextField holder;

    public PermissionValidator(JTextField holder) {
        this.holder = holder;
    }

    @Override
    public ValidationResult validate() {
        String text = this.holder.getText();
        if (!Pattern.compile("^[a-z.]+$").matcher(text).matches()) {
            new ValidationResult(ValidationResult.Type.ERROR, L10N.t("validators.bukkit_permission"));
        }

        return ValidationResult.PASSED;
    }
}
