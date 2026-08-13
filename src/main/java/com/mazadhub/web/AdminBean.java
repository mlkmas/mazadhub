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

/** Admin dashboard: manage categories, view users, watch active auctions. */
@Named
@RequestScoped
public class AdminBean {

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

    @PostConstruct
    public void init() {
        refresh();
    }

    /** Redirect non-admins away before the page renders. */
    public String guard() {
        return session.isAdmin() ? null : "catalog?faces-redirect=true";
    }

    private void refresh() {
        categoryList = items.listCategories();
        users = queries.allUsers();
        activeAuctions = queries.allActiveItems();
    }

    public long itemsIn(Category c) {
        return queries.itemCountByCategory(c.getId());
    }

    @Transactional
    public void addCategory() {
        if (newCategoryName == null || newCategoryName.isBlank()) {
            error("Category name is required.");
            return;
        }
        categories.save(new Category(newCategoryName.trim(),
                newCategoryDescription == null ? "" : newCategoryDescription.trim()));
        newCategoryName = null;
        newCategoryDescription = null;
        refresh();
        info("Category added.");
    }

    @Transactional
    public void promoteToAdmin(User u) {
        User managed = userRepo.findById(u.getId()).orElse(null);
        if (managed != null) {
            managed.setRole(UserRole.ADMIN);
            userRepo.save(managed);
            refresh();
            info(u.getUsername() + " is now an admin.");
        }
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void error(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    public List<Category> getCategoryList() { return categoryList; }
    public List<User> getUsers() { return users; }
    public List<Item> getActiveAuctions() { return activeAuctions; }
    public String getNewCategoryName() { return newCategoryName; }
    public void setNewCategoryName(String v) { this.newCategoryName = v; }
    public String getNewCategoryDescription() { return newCategoryDescription; }
    public void setNewCategoryDescription(String v) { this.newCategoryDescription = v; }
}
