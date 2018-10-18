package beans;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;

@ManagedBean
@SessionScoped
public class LocaleChanger implements Serializable {

    private Locale currentLocale; // = FacesContext.getCurrentInstance().getViewRoot().getLocale();

    @PostConstruct
    public void init() {
        currentLocale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }

    public LocaleChanger() {
    }

    public void changeLocale(String localeCode) {
        currentLocale = new Locale(localeCode);
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }
}
