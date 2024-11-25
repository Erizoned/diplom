package controllers;
import com.college.receipt.Recipe;
import service.RecipeService;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;


@RequestMapping("/recipes")
@org.springframework.stereotype.Controller
public class RecipeController {
    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService){
        this.recipeService = recipeService;
    }

    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }

    @GetMapping("/{id}/recipe")
    public String recipe(@PathVariable("id") Long id, Model model){
        Recipe recipe = recipeService.findRecipeById(id).orElseThrow(() -> new RuntimeException("Recipe not found"));
        model.addAttribute("recipe", recipe);
        return "recipe";
    }

    @GetMapping("/create_recipe")
    public String createForm(Model model){
        model.addAttribute("recipe", new Recipe());
        return "create_recipe";
    }

    @PostMapping("/create_recipe")
    public String createRecipe(@Valid @ModelAttribute Recipe newRecipe,
                               BindingResult result,
                               @RequestParam("photoFood") MultipartFile photoFile,
                               @RequestParam("stepPhoto") MultipartFile stepFile) throws IOException {

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            return "create_recipe";
        }

        // Устанавливаем фото, если файл не пуст
//        if (!photoFile.isEmpty()) {
//            newRecipe.setPhotoFood(photoFile.getBytes());
//        }
//
//        if (!stepFile.isEmpty()) {
//            newRecipe.setStepPhoto(stepFile.getBytes());
//        }

        // Сохраняем рецепт
        recipeService.createRecipe(newRecipe);
        return "redirect:/recipes";
    }


    @PostMapping("/delete_recipe/{id}")
    public ResponseEntity<Recipe> deleteRecipeById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(recipeService.deleteRecipeById(id));
    }

//    @PostMapping("/{id}/photo")
//    public ResponseEntity<String> uploadPhoto(@PathVariable("id") Long id, @RequestParam("photo") MultipartFile file) throws IOException{
//        Recipe recipe = recipeService.findRecipeById(id).orElseThrow(() -> new RuntimeException("Recipe not found"));
//        recipe.setPhotoFood(file.getBytes());
//        recipeService.updateRecipe(recipe);
//        return ResponseEntity.ok("Фото загружено");
//    }

//    @GetMapping("/{id}/photo")
//    public ResponseEntity<byte[]> getPhoto(@PathVariable("id") Long id){
//        Recipe recipe = recipeService.findRecipeById(id).orElseThrow(() -> new RuntimeException());
//        byte[] photo = recipe.getPhotoFood();
//        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG) .body(photo);
//    }

    @PostMapping("/{id}/step_photo")
    public ResponseEntity<String> uploadStepPhoto(@PathVariable("id") Long id, @RequestParam("photoRecipe") MultipartFile file) throws IOException{
        Recipe recipe = recipeService.findRecipeById(id).orElseThrow(() -> new RuntimeException("Recipe not found"));
        recipe.setStepPhoto(file.getBytes());
        recipeService.updateRecipe(recipe);
        return ResponseEntity.ok("Фото загружено");
    }

    @GetMapping("/{id}/stepPhoto")
    public ResponseEntity<byte[]> getstepPhoto(@PathVariable("id") Long id){
        Recipe recipe = recipeService.findRecipeById(id).orElseThrow(() -> new RuntimeException());
        byte[] photo = recipe.getStepPhoto();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG) .body(photo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> findRecipeById(@PathVariable("id") Long id){
        return recipeService.findRecipeById(id)
                .map(recipe -> ResponseEntity.ok(recipe))
                .orElseThrow(() -> new RecipeNotFoundException("Рецепт с айди " + id + " не найден"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable("id") Long id, @RequestBody Recipe recipe){
        return ResponseEntity.ok(recipeService.updateRecipe(recipe));
    }

    public class RecipeNotFoundException extends RuntimeException{
        public RecipeNotFoundException(String message){
            super(message);
        }
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> getRecipes(){
        return ResponseEntity.ok(recipeService.findAllRecipes());
    }
}
