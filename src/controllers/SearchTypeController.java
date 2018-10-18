package controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@ManagedBean
@RequestScoped
public class SearchTypeController implements Serializable {

    private Map<String, SearchType> searchList = new HashMap<>();

    public SearchTypeController() {

        ResourceBundle bundle = ResourceBundle.getBundle("nls.t.messages", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchList.put(bundle.getString("author"), SearchType.AUTHOR);
        searchList.put(bundle.getString("named"), SearchType.AUTHOR);
    }

    public Map<String, SearchType> getSearchList() {
        return searchList;
    }
}
