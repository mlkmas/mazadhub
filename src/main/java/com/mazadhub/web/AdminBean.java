package com.mazadhub.web;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.repository.CategoryRepository;
import com.mazadhub.repository.UserRepository;
import com.mazadhub.service.ItemService;
import com.mazadhub.web.support.WebQueries;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.util.List;

// Admin dashboard: categories, users and the list of running auctions
@Named
@RequestScoped
public class AdminBean
{
    // services, repositories and queries the dashboard reads through
    @Inject
    private ItemService items;

    @Inject
    private CategoryRepository categories;

    @Inject
    private UserRepository userRepo;

    @Inject
    private WebQueries queries;

    @Inject
    private SessionBean session;

    private List<Category> categoryList;
    private List<User> users;
    private List<Item> activeAuctions;

    private String newCategoryName;
    private String newCategoryDescription;

    // Fills the three tables when the bean is created
    @PostConstruct
    public void init()
    {
        refresh();
    }

    // Sends anyone who is not an admin back to the catalogue
    public String guard()
    {
        return session.isAdmin()?null:"catalog?faces-redirect=true";
    }

    // Re-reads the three tables after a change
    private void refresh()
    {
        categoryList=items.listCategories();
        users=queries.allUsers();
        activeAuctions=queries.allActiveItems();
    }

    // How many items sit in one category
    public long itemsIn(Category c)
    {
        return queries.itemCountByCategory(c.getId());
    }

    // Adds the typed category, refusing an empty name
    @Transactional
    public void addCategory()
    {
        if(newCategoryName==null||newCategoryName.isBlank())
        {
            error("Category name is required.");
            return;
        }

        categories.save(new Category(newCategoryName.trim(),
                newCategoryDescription==null?"":newCategoryDescription.trim()));
        newCategoryName=null;
        newCategoryDescription=null;
        refresh();
        info("Category added.");
    }

    // Gives another user the admin role
    @Transactional
    public void promoteToAdmin(User u)
    {
        User managed=userRepo.findById(u.getId()).orElse(null);
        if(managed!=null)
        {
            managed.setRole(UserRole.ADMIN);
            userRepo.save(managed);
            refresh();
            info(u.getUsername()+" is now an admin.");
        }
    }

    // Shows a green message on the page
    private void info(String m)
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    // Shows a red message on the page
    private void error(String m)
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    // getters / setters used by the JSF pages and services
    public List<Category> getCategoryList()
    {
        return categoryList;
    }

    public List<User> getUsers()
    {
        return users;
    }

    public List<Item> getActiveAuctions()
    {
        return activeAuctions;
    }

    public String getNewCategoryName()
    {
        return newCategoryName;
    }

    public void setNewCategoryName(String v)
    {
        this.newCategoryName=v;
    }

    public String getNewCategoryDescription()
    {
        return newCategoryDescription;
    }

    public void setNewCategoryDescription(String v)
    {
        this.newCategoryDescription=v;
    }
}
