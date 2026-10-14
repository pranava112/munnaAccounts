package com.vpm.Accounts.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tenants", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "default_chart_initialized", nullable = false, columnDefinition = "boolean default false")
    private boolean defaultChartInitialized;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public boolean isDefaultChartInitialized() { return defaultChartInitialized; }
    public void setDefaultChartInitialized(boolean defaultChartInitialized) {
        this.defaultChartInitialized = defaultChartInitialized;
    }
}
