package validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.ResourceBundle;

@FacesValidator("loginValidator")
public class LoginValidator implements Validator {
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object value) throws ValidatorException {

        ResourceBundle bundle = ResourceBundle.getBundle("nls.t.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());

        String newValue = value.toString();
        try {
            if (!Character.isLetter(newValue.charAt(0))) {
                throw new IllegalArgumentException(bundle.getString("input_err_letter"));
            }
            if (newValue.length() < 3) {
                throw new IllegalArgumentException(bundle.getString("input_err_length"));
            }
            if (getTestArray().contains(newValue)) {
                throw new IllegalArgumentException(bundle.getString("used_name"));
            }
        } catch (IllegalArgumentException e) {
            FacesMessage message = new FacesMessage(e.getMessage());
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }

    private ArrayList<String> getTestArray() {
        ArrayList<String> list = new ArrayList<>();
        list.add("username");
        list.add("login");
        return list;
    }
}
