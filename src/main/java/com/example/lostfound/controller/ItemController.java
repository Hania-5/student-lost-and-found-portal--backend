package com.example.lostfound.controller;

import com.example.lostfound.model.Item;
import com.example.lostfound.repository.ItemRepository;
import com.example.lostfound.security.AdminTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemRepository repo;

    @Autowired
    private AdminTokenStore tokens;

    // ----- helpers -----

    private boolean isAdmin(String authHeader) {
        return authHeader != null
                && authHeader.startsWith("Bearer ")
                && tokens.isValid(authHeader.substring(7));
    }

    private ResponseEntity<Object> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Admin login required"));
    }

    private ResponseEntity<Object> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Item not found"));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    // Returns an error message, or null when the item is valid
    private String validate(Item item) {
        if (blank(item.getItemName())) return "Item name is required";
        if (blank(item.getDescription())) return "Description is required";
        if (blank(item.getCategory())) return "Category is required";
        if (blank(item.getLocation())) return "Location is required";
        if (blank(item.getContactInfo())) return "Contact information is required";

        if (blank(item.getType())) return "Type must be LOST or FOUND";

        String type = item.getType().trim().toUpperCase();

        if (!type.equals("LOST") && !type.equals("FOUND")) {
            return "Type must be LOST or FOUND";
        }

        item.setType(type);

        // Photos are stored as text, so keep them reasonably small
        if (item.getImage() != null && item.getImage().length() > 3_000_000) {
            return "Photo is too large";
        }

        return null;
    }

    // ----- anyone can view and report items -----

    @GetMapping
    public List<Item> getAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        Optional<Item> item = repo.findById(id);

        if (item.isEmpty()) {
            return notFound();
        }

        return ResponseEntity.ok(item.get());
    }

    @GetMapping("/type/{type}")
    public List<Item> getByType(@PathVariable String type) {
        return repo.findByType(type.toUpperCase());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Item item) {
        String error = validate(item);

        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        item.setId(null);
        item.setStatus("OPEN");

        if (item.getDate() == null) {
            item.setDate(LocalDate.now());
        }

        return ResponseEntity.ok(repo.save(item));
    }

    // ----- admin only: edit -----

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Item item,
            @RequestHeader(value = "Authorization", required = false)
            String authHeader) {

        if (!isAdmin(authHeader)) {
            return unauthorized();
        }

        Optional<Item> existing = repo.findById(id);

        if (existing.isEmpty()) {
            return notFound();
        }

        String error = validate(item);

        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        item.setId(id);

        if (item.getDate() == null) {
            item.setDate(LocalDate.now());
        }

        if (blank(item.getStatus())) {
            item.setStatus("OPEN");
        }

        return ResponseEntity.ok(repo.save(item));
    }

    // ----- students can claim an eligible FOUND item -----

    @PutMapping("/{id}/claim")
    public ResponseEntity<?> markClaimed(@PathVariable Long id) {

        Optional<Item> found = repo.findById(id);

        if (found.isEmpty()) {
            return notFound();
        }

        Item item = found.get();

        // Only FOUND items can be claimed
        if (blank(item.getType())
                || !item.getType().trim().equalsIgnoreCase("FOUND")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only FOUND items can be claimed."));
        }

        // Do not allow an item to be claimed twice
        if (blank(item.getStatus())
                || !item.getStatus().trim().equalsIgnoreCase("OPEN")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "This item is already claimed or is not open."));
        }

        // A matching OPEN LOST item must exist in the same category
        if (blank(item.getCategory())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "The found item has no category."));
        }

        List<Item> matches =
                repo.findByTypeIgnoreCaseAndCategoryIgnoreCaseAndStatusIgnoreCase(
                        "LOST",
                        item.getCategory().trim(),
                        "OPEN"
                );

        if (matches.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "No matching OPEN LOST item exists in this category."
                    ));
        }

        item.setStatus("CLAIMED");

        return ResponseEntity.ok(repo.save(item));
    }

    // ----- admin only: delete -----

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false)
            String authHeader) {

        if (!isAdmin(authHeader)) {
            return unauthorized();
        }

        if (!repo.existsById(id)) {
            return notFound();
        }

        repo.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}