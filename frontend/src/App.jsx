import React, { useEffect, useMemo, useState } from 'react';
import { ChefHat, Search, LogOut, Home, Plus, Pencil, Trash2, X, UtensilsCrossed, Heart, Clock3, ShieldCheck } from 'lucide-react';
import api from './api';

const blankRecipe = { title: '', description: '', ingredients: '', instructions: '', imageUrl: '', category: '', prepMinutes: 20 };

function readSession() {
  try {
    const user = JSON.parse(localStorage.getItem('cookwish_user') || 'null');
    const token = localStorage.getItem('cookwish_token');
    return token && user ? { token, user } : null;
  } catch { return null; }
}

export default function App() {
  const [session, setSession] = useState(readSession);
  const [page, setPage] = useState(session ? 'home' : 'login');
  const [login, setLogin] = useState({ username: '', password: '' });
  const [loginError, setLoginError] = useState('');
  const [recipes, setRecipes] = useState([]);
  const [search, setSearch] = useState('');
  const [form, setForm] = useState(blankRecipe);
  const [editingId, setEditingId] = useState(null);
  const [notice, setNotice] = useState('');
  const isAdmin = session?.user?.role === 'ADMIN';

  async function loadRecipes() {
    try {
      const response = await api.get('/recipes');
      setRecipes(response.data);
    } catch (error) {
      setNotice(error.response?.data?.message || 'Could not load recipes. Check that the backend and MySQL are running.');
    }
  }

  useEffect(() => {
    if (session && page === 'home') loadRecipes();
  }, [session, page]);

  const filteredRecipes = useMemo(() => {
    const q = search.trim().toLowerCase();
    return recipes.filter(r => [r.title, r.description, r.category, r.ingredients].some(v => (v || '').toLowerCase().includes(q)));
  }, [recipes, search]);

  async function handleLogin(e) {
    e.preventDefault();
    setLoginError('');
    try {
      const response = await api.post('/auth/login', login);
      const nextSession = { token: response.data.token, user: { username: response.data.username, role: response.data.role } };
      localStorage.setItem('cookwish_token', nextSession.token);
      localStorage.setItem('cookwish_user', JSON.stringify(nextSession.user));
      setSession(nextSession);
      setPage('home');
      setNotice(`Welcome back, ${nextSession.user.username}!`);
    } catch (error) {
      setLoginError(error.response?.data?.message || 'Login failed. Check your username and password.');
    }
  }

  function logout() {
    localStorage.removeItem('cookwish_token');
    localStorage.removeItem('cookwish_user');
    setSession(null);
    setPage('logout');
    setRecipes([]);
  }

  function startEdit(recipe) {
    setEditingId(recipe.id);
    setForm({
      title: recipe.title || '',
      description: recipe.description || '',
      ingredients: recipe.ingredients || '',
      instructions: recipe.instructions || '',
      imageUrl: recipe.imageUrl || '',
      category: recipe.category || '',
      prepMinutes: recipe.prepMinutes || 20
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function resetForm() {
    setEditingId(null);
    setForm(blankRecipe);
  }

  async function saveRecipe(e) {
    e.preventDefault();
    try {
      const payload = { ...form, prepMinutes: Number(form.prepMinutes) || 0 };
      if (editingId) await api.put(`/recipes/${editingId}`, payload);
      else await api.post('/recipes', payload);
      setNotice(editingId ? 'Recipe updated successfully.' : 'Recipe created successfully.');
      resetForm();
      await loadRecipes();
    } catch (error) {
      setNotice(error.response?.data?.message || 'Could not save recipe. Check your admin login and backend.');
    }
  }

  async function deleteRecipe(id) {
    if (!window.confirm('Delete this recipe?')) return;
    try {
      await api.delete(`/recipes/${id}`);
      setRecipes(old => old.filter(r => r.id !== id));
      setNotice('Recipe deleted.');
    } catch (error) {
      setNotice(error.response?.data?.message || 'Could not delete recipe.');
    }
  }

  if (!session && page === 'login') {
    return <main className="auth-shell">
      <div className="auth-art">
        <div className="brand brand-light"><span className="brand-mark"><ChefHat size={23} /></span> CookWish</div>
        <div className="auth-art-copy">
          <span className="eyebrow">A little joy in every dish</span>
          <h1>Good food.<br /><em>Good mood.</em></h1>
          <p>Find your next favourite recipe, one delicious idea at a time.</p>
        </div>
        <img className="auth-food" src="https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=900&q=85" alt="Fresh colourful meal" />
        <div className="floating-note"><Heart size={16} fill="currentColor" /> Made with a little love</div>
      </div>
      <section className="auth-panel">
        <div className="auth-card">
          <div className="mobile-brand brand"><span className="brand-mark"><ChefHat size={23} /></span> CookWish</div>
          <span className="eyebrow">WELCOME TO COOKWISH</span>
          <h2>Let’s get cooking.</h2>
          <p className="muted">Sign in to discover recipes made for you.</p>
          <form onSubmit={handleLogin} className="login-form">
            <label>Username<input value={login.username} onChange={e => setLogin({ ...login, username: e.target.value })} placeholder="Enter your username" autoComplete="username" required /></label>
            <label>Password<input type="password" value={login.password} onChange={e => setLogin({ ...login, password: e.target.value })} placeholder="Enter your password" autoComplete="current-password" required /></label>
            {loginError && <div className="error-msg">{loginError}</div>}
            <button className="primary-btn full-btn" type="submit">Sign in <span>→</span></button>
          </form>
          <div className="demo-box"><strong>Demo accounts</strong><span>Admin: admin / Admin@123</span><span>User: user / User@123</span></div>
          <p className="auth-foot">Fresh ideas are just one sign-in away.</p>
        </div>
      </section>
    </main>;
  }

  if (page === 'logout') {
    return <main className="logout-shell">
      <div className="logout-card">
        <div className="logout-icon"><LogOut size={30} /></div>
        <span className="eyebrow">SESSION ENDED</span>
        <h1>You’ve logged out.</h1>
        <p className="muted">Thanks for stopping by CookWish. Your next delicious idea will be waiting.</p>
        <button className="primary-btn" onClick={() => { setPage('login'); setLogin({ username: '', password: '' }); }}>Back to login <span>→</span></button>
      </div>
    </main>;
  }

  return <div className="app-shell">
    <header className="navbar">
      <button className="brand brand-button" onClick={() => setPage('home')}><span className="brand-mark"><ChefHat size={22} /></span> CookWish<span className="brand-dot">.</span></button>
      <nav><button className={`nav-link ${page === 'home' ? 'active' : ''}`} onClick={() => setPage('home')}><Home size={16} /> Home</button></nav>
      <div className="nav-right">
        <span className={`role-pill ${isAdmin ? 'admin-pill' : ''}`}>{isAdmin ? <ShieldCheck size={14} /> : <Heart size={14} />} {isAdmin ? 'Admin' : 'Recipe lover'}</span>
        <span className="user-greeting">Hi, {session?.user?.username}</span>
        <button className="logout-btn" onClick={logout}><LogOut size={16} /> Logout</button>
      </div>
    </header>

    <main className="main-content">
      <section className="hero">
        <div className="hero-copy">
          <span className="eyebrow"><span className="eyebrow-line"></span> YOUR DAILY DOSE OF DELICIOUS</span>
          <h1>Find something<br /> <em>wonderful</em> to cook.</h1>
          <p>Simple ingredients, lovely flavours, and recipes worth sharing.</p>
          <div className="hero-stats"><span><UtensilsCrossed size={16} /> Recipes for every mood</span><span><Heart size={16} /> Made to be loved</span></div>
        </div>
        <div className="hero-image-wrap">
          <img src="https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=1000&q=85" alt="A bowl of fresh food" />
          <div className="hero-image-tag"><span>✦</span> Your kitchen, your happy place</div>
        </div>
        <div className="hero-scribble">eat well,<br />feel good ♡</div>
      </section>

      {notice && <div className="notice"><span>{notice}</span><button onClick={() => setNotice('')} aria-label="Dismiss notice"><X size={16} /></button></div>}

      {isAdmin && <section className="admin-panel">
        <div className="section-heading admin-heading">
          <div><span className="eyebrow">ADMIN WORKSPACE</span><h2>{editingId ? 'Edit a recipe' : 'Add a new recipe'}</h2><p className="muted">Keep your recipe collection fresh and inspiring.</p></div>
          <span className="admin-badge"><ShieldCheck size={16} /> Admin access</span>
        </div>
        <form className="recipe-form" onSubmit={saveRecipe}>
          <label>Recipe name<input required value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="e.g. Creamy tomato pasta" /></label>
          <label>Category<input value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} placeholder="e.g. Dinner, Dessert" /></label>
          <label className="form-wide">Short description<textarea required rows="2" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="What makes this recipe special?" /></label>
          <label>Ingredients<textarea rows="3" value={form.ingredients} onChange={e => setForm({ ...form, ingredients: e.target.value })} placeholder="List ingredients, separated by commas" /></label>
          <label>Instructions<textarea rows="3" value={form.instructions} onChange={e => setForm({ ...form, instructions: e.target.value })} placeholder="Step-by-step cooking instructions" /></label>
          <label>Image URL<input type="url" value={form.imageUrl} onChange={e => setForm({ ...form, imageUrl: e.target.value })} placeholder="https://example.com/recipe.jpg" /></label>
          <label>Prep time (minutes)<input type="number" min="0" value={form.prepMinutes} onChange={e => setForm({ ...form, prepMinutes: e.target.value })} /></label>
          <div className="form-actions form-wide">
            <button className="primary-btn" type="submit"><Plus size={17} /> {editingId ? 'Save changes' : 'Add recipe'}</button>
            {editingId && <button className="secondary-btn" type="button" onClick={resetForm}>Cancel edit</button>}
          </div>
        </form>
      </section>}

      <section className="recipes-section">
        <div className="section-heading recipe-heading">
          <div><span className="eyebrow">THE RECIPE COLLECTION</span><h2>Made with love, served with joy.</h2><p className="muted">A few delicious ideas for whatever you’re craving.</p></div>
          <div className="recipe-count"><strong>{filteredRecipes.length.toString().padStart(2, '0')}</strong><span>recipes to explore</span></div>
        </div>
        <div className="search-row"><div className="search-box"><Search size={19} /><input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search recipes, ingredients, or categories..." /><kbd>⌕</kbd></div><span className="search-hint">Find your next favourite</span></div>
        {filteredRecipes.length ? <div className="recipe-grid">{filteredRecipes.map((recipe, index) => <article className="recipe-card" key={recipe.id}>
          <div className="recipe-image-wrap">
            <img src={recipe.imageUrl || 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=700&q=80'} alt={recipe.title} onError={e => { e.currentTarget.src = 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=700&q=80'; }} />
            <span className="category-chip">{recipe.category || 'CookWish pick'}</span>
            {isAdmin && <div className="card-actions"><button title="Edit recipe" onClick={() => startEdit(recipe)}><Pencil size={15} /></button><button title="Delete recipe" onClick={() => deleteRecipe(recipe.id)}><Trash2 size={15} /></button></div>}
          </div>
          <div className="recipe-card-body"><div className="recipe-meta"><span><Clock3 size={14} /> {recipe.prepMinutes || 20} min</span><span>♡ CookWish</span></div><h3>{recipe.title}</h3><p>{recipe.description}</p>{recipe.ingredients && <div className="ingredients"><strong>Ingredients</strong><span>{recipe.ingredients}</span></div>}</div>
        </article>)}</div> : <div className="empty-state"><div>🥣</div><h3>No recipes found just yet</h3><p>Try another search, or ask your admin to add a recipe.</p></div>}
      </section>
      <footer className="footer"><span className="brand-mini"><ChefHat size={17} /> CookWish</span><span>Made with a little love, for everyday cooking. ♡</span></footer>
    </main>
  </div>;
}
