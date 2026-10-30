package com.cookwish.resource;

import com.cookwish.model.ApiMessage;
import com.cookwish.model.Recipe;
import com.cookwish.util.Database;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/recipes")
public class RecipeResource {
    @GET @Produces(MediaType.APPLICATION_JSON)
    public Response allRecipes() {
        String sql = "SELECT id,title,description,ingredients,instructions,image_url,category,prep_minutes FROM recipes ORDER BY id DESC";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Recipe> recipes = new ArrayList<>();
            while (rs.next()) recipes.add(fromRow(rs));
            return Response.ok(recipes).build();
        } catch (Exception e) { return serverError("Could not read recipes. Check database settings and schema."); }
    }

    @GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Response getRecipe(@PathParam("id") long id) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT id,title,description,ingredients,instructions,image_url,category,prep_minutes FROM recipes WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Response.status(404).entity(new ApiMessage("Recipe not found")).build();
                return Response.ok(fromRow(rs)).build();
            }
        } catch (Exception e) { return serverError("Could not read recipe."); }
    }

    @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response createRecipe(Recipe recipe) {
        if (!valid(recipe)) return Response.status(400).entity(new ApiMessage("Recipe name and description are required")).build();
        String sql = "INSERT INTO recipes(title,description,ingredients,instructions,image_url,category,prep_minutes) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, recipe); ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) recipe.setId(keys.getLong(1)); }
            return Response.status(201).entity(recipe).build();
        } catch (Exception e) { return serverError("Could not create recipe."); }
    }

    @PUT @Path("/{id}") @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    public Response updateRecipe(@PathParam("id") long id, Recipe recipe) {
        if (!valid(recipe)) return Response.status(400).entity(new ApiMessage("Recipe name and description are required")).build();
        String sql = "UPDATE recipes SET title=?,description=?,ingredients=?,instructions=?,image_url=?,category=?,prep_minutes=? WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, recipe); ps.setLong(8, id);
            if (ps.executeUpdate() == 0) return Response.status(404).entity(new ApiMessage("Recipe not found")).build();
            recipe.setId(id); return Response.ok(recipe).build();
        } catch (Exception e) { return serverError("Could not update recipe."); }
    }

    @DELETE @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRecipe(@PathParam("id") long id) {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM recipes WHERE id=?")) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) return Response.status(404).entity(new ApiMessage("Recipe not found")).build();
            return Response.ok(new ApiMessage("Recipe deleted")).build();
        } catch (Exception e) { return serverError("Could not delete recipe."); }
    }

    // JAXB-backed XML routes for XML clients. JAX-RS still maps the HTTP endpoints.
    @GET @Path("/xml") @Produces(MediaType.APPLICATION_XML)
    public Response allRecipesXml() {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT id,title,description,ingredients,instructions,image_url,category,prep_minutes FROM recipes ORDER BY id DESC"); ResultSet rs = ps.executeQuery()) {
            RecipeList list = new RecipeList();
            while (rs.next()) list.getRecipes().add(fromRow(rs));
            return Response.ok(list).build();
        } catch (Exception e) { return serverError("Could not read XML recipes."); }
    }

    @POST @Path("/xml") @Consumes(MediaType.APPLICATION_XML) @Produces(MediaType.APPLICATION_XML)
    public Response createRecipeXml(Recipe recipe) {
        if (!valid(recipe)) return Response.status(400).entity(new ApiMessage("Recipe name and description are required")).build();
        String sql = "INSERT INTO recipes(title,description,ingredients,instructions,image_url,category,prep_minutes) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, recipe); ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) recipe.setId(keys.getLong(1)); }
            return Response.status(201).entity(recipe).build();
        } catch (Exception e) { return serverError("Could not create XML recipe."); }
    }

    private Recipe fromRow(ResultSet rs) throws SQLException {
        Recipe r = new Recipe(); r.setId(rs.getLong("id")); r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description")); r.setIngredients(rs.getString("ingredients"));
        r.setInstructions(rs.getString("instructions")); r.setImageUrl(rs.getString("image_url"));
        r.setCategory(rs.getString("category")); r.setPrepMinutes(rs.getInt("prep_minutes")); return r;
    }
    private void bind(PreparedStatement ps, Recipe r) throws SQLException {
        ps.setString(1, r.getTitle().trim()); ps.setString(2, r.getDescription().trim());
        ps.setString(3, r.getIngredients()); ps.setString(4, r.getInstructions());
        ps.setString(5, r.getImageUrl()); ps.setString(6, r.getCategory()); ps.setInt(7, Math.max(0, r.getPrepMinutes()));
    }
    private boolean valid(Recipe r) { return r != null && r.getTitle() != null && !r.getTitle().trim().isEmpty() && r.getDescription() != null && !r.getDescription().trim().isEmpty(); }
    private Response serverError(String message) { return Response.status(500).entity(new ApiMessage(message)).build(); }

    @javax.xml.bind.annotation.XmlRootElement(name = "recipes")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    public static class RecipeList {
        private List<Recipe> recipes = new ArrayList<>();
        public RecipeList() {}
        public List<Recipe> getRecipes() { return recipes; }
        public void setRecipes(List<Recipe> recipes) { this.recipes = recipes; }
    }
}
