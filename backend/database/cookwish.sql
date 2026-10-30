CREATE DATABASE IF NOT EXISTS cookwish_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cookwish_db;

CREATE TABLE IF NOT EXISTS recipes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(180) NOT NULL,
  description TEXT NOT NULL,
  ingredients TEXT,
  instructions TEXT,
  image_url VARCHAR(1000),
  category VARCHAR(100),
  prep_minutes INT NOT NULL DEFAULT 20,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO recipes (title, description, ingredients, instructions, image_url, category, prep_minutes)
SELECT 'Creamy Tomato Pasta', 'A cosy bowl of pasta with a silky tomato sauce and fresh herbs.',
       'Pasta, tomatoes, garlic, cream, olive oil, basil, salt and pepper',
       'Boil pasta. Sauté garlic and tomatoes, stir in cream, then toss with pasta and basil.',
       'https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=800&q=85', 'Dinner', 25
WHERE NOT EXISTS (SELECT 1 FROM recipes WHERE title = 'Creamy Tomato Pasta');

INSERT INTO recipes (title, description, ingredients, instructions, image_url, category, prep_minutes)
SELECT 'Fresh Garden Salad', 'A crisp, colourful salad with a bright lemon dressing.',
       'Lettuce, cucumber, tomatoes, onion, lemon, olive oil, salt and pepper',
       'Chop the vegetables, whisk lemon with olive oil and seasoning, then toss together.',
       'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=800&q=85', 'Healthy', 15
WHERE NOT EXISTS (SELECT 1 FROM recipes WHERE title = 'Fresh Garden Salad');

INSERT INTO recipes (title, description, ingredients, instructions, image_url, category, prep_minutes)
SELECT 'Berry Breakfast Bowl', 'A refreshing bowl topped with berries, seeds and creamy yoghurt.',
       'Yoghurt, strawberries, blueberries, banana, oats, honey and seeds',
       'Add yoghurt to a bowl, top with fruit, oats and seeds, and drizzle with honey.',
       'https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=800&q=85', 'Breakfast', 10
WHERE NOT EXISTS (SELECT 1 FROM recipes WHERE title = 'Berry Breakfast Bowl');
