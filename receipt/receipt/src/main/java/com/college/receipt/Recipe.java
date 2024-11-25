package com.college.receipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.Type;

@Table(name = "recipes")
@Entity
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @NotBlank(message = "Описание рецепта не может быть пустым")
    private String description;

    @NotBlank(message = "Блюдо не может быть без темы")
    private String theme;

    @Column(name = "stepPhoto")
    private byte[] stepPhoto;

    @Column(name = "typeOfFood")
    private String typeOfFood;

    @Column(name = "nationalKitchen")
    private String nationalKitchen;

    @Column(name = "typeOfCook")
    private String typeOfCook;

    @Column(name = "restrictions")
    private String restrictions;

    @Column(name = "countPortion")
    private int countPortion;

    @Column(name = "kkal")
    private int kkal;

    @Column(name = "timeToCook")
    private int timeToCook;

    public Recipe() {}

    public Recipe(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getTheme() {
        return theme;
    }

    public int getKkal() {
        return kkal;
    }

    public void setKkal(int kkal) {
        this.kkal = kkal;
    }

    public String getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }

    public String getTypeOfCook() {
        return typeOfCook;
    }

    public void setTypeOfCook(String typeOfCook) {
        this.typeOfCook = typeOfCook;
    }

    public int getTimeToCook() {
        return timeToCook;
    }

    public void setTimeToCook(int timeToCook) {
        this.timeToCook = timeToCook;
    }

    public void setCountPortion(int countPortion) {
        this.countPortion = countPortion;
    }

    public int getCountPortion() {
        return countPortion;
    }

    public void setNationalKitchen(String nationalKitchen) {
        this.nationalKitchen = nationalKitchen;
    }

    public String getNationalKitchen() {
        return nationalKitchen;
    }

    public void setTypeOfFood(String typeOfFood) {
        this.typeOfFood = typeOfFood;
    }

    public String getTypeOfFood() {
        return typeOfFood;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public byte[] getStepPhoto() {
        return stepPhoto;
    }

    public void setStepPhoto(byte[] stepPhoto) {
        this.stepPhoto = stepPhoto;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
