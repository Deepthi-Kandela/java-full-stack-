/* =====================================================================
   Think Mart - shared frontend logic (talks to the Spring Boot REST API)
   One file, used by every page. What runs is decided by the page name.
   ===================================================================== */
(function () {
  'use strict';

  // ---------------------------------------------------------------- config
  // Same origin by default (the Spring Boot app serves these pages).
  // If you open the HTML with VS Code Live Server / file://, talk to localhost:8080.
  var isLocalDev =
    location.protocol === 'file:' ||
    ((location.hostname === 'localhost' || location.hostname === '127.0.0.1') && location.port !== '8080');
  var API_BASE = typeof window.API_BASE === 'string' ? window.API_BASE : (isLocalDev ? 'http://localhost:8080' : '');

  var TOKEN_KEY = 'tm_token';
  var USER_KEY = 'tm_user';
  var page = (location.pathname.split('/').pop() || '').toLowerCase();
  var isPublicPage = document.body.hasAttribute('data-public');

  var store = {
    get token() { return localStorage.getItem(TOKEN_KEY); },
    get user() {
      try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch (e) { return null; }
    },
    save: function (token, user) {
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    },
    clear: function () {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  };

  // ---------------------------------------------------------------- helpers
  function $(id) { return document.getElementById(id); }

  function esc(v) {
    return String(v == null ? '' : v)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }

  function money(n) {
    return '\u20B9' + Number(n || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 });
  }

  function fmtDate(s) {
    var d = new Date(s);
    return isNaN(d) ? '' : d.toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
  }

  function assetUrl(u) {
    if (!u) return '';
    return /^https?:/i.test(u) ? u : API_BASE + u;
  }

  function placeholder(label) {
    var svg = '<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300">' +
      '<rect width="100%" height="100%" fill="#e8f1fa"/>' +
      '<text x="50%" y="50%" font-family="Verdana" font-size="22" fill="#26558b" ' +
      'text-anchor="middle" dominant-baseline="middle">' + esc(label) + '</text></svg>';
    return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
  }

  function imgSrc(p) {
    return p.imageUrl ? assetUrl(p.imageUrl) : placeholder(p.brand);
  }

  // Any image that fails to load (e.g. photo not added yet) gets a neat placeholder
  document.addEventListener('error', function (e) {
    var t = e.target;
    if (t && t.tagName === 'IMG' && !t.dataset.fallback) {
      t.dataset.fallback = '1';
      t.src = placeholder(t.alt || 'Think Mart');
    }
  }, true);

  function toast(message, isError) {
    var box = $('toast-box');
    if (!box) {
      box = document.createElement('div');
      box.id = 'toast-box';
      document.body.appendChild(box);
    }
    var t = document.createElement('div');
    t.className = 'toast' + (isError ? ' error' : '');
    t.textContent = message;
    box.appendChild(t);
    setTimeout(function () { t.remove(); }, 3200);
  }

  function setMsg(text, ok) {
    var el = $('formMsg');
    if (!el) return;
    el.textContent = text || '';
    if (ok) el.style.color = '#2e7d32';
  }

  // ---------------------------------------------------------------- API
  async function api(path, opts) {
    opts = opts || {};
    var headers = opts.headers || {};
    if (store.token) headers['Authorization'] = 'Bearer ' + store.token;

    var body = opts.body;
    if (body !== undefined && !(body instanceof FormData) && typeof body !== 'string') {
      body = JSON.stringify(body);
      headers['Content-Type'] = 'application/json';
    }

    var res;
    try {
      res = await fetch(API_BASE + path, { method: opts.method || 'GET', headers: headers, body: body });
    } catch (e) {
      throw new Error('Cannot reach the server. Is the Spring Boot backend running?');
    }

    var data = null;
    var text = await res.text();
    if (text) {
      try { data = JSON.parse(text); } catch (e) { data = { message: text }; }
    }

    if (res.status === 401 && !isPublicPage) {
      store.clear();
      location.replace('Login-Page.html');
      throw new Error('Please log in again');
    }
    if (!res.ok) {
      throw new Error((data && data.message) || ('Request failed (' + res.status + ')'));
    }
    return data;
  }

  // ---------------------------------------------------------------- guard + nav
  if (!isPublicPage && !store.token) {
    location.replace('Login-Page.html');
    return;
  }

  function initNav() {
    // Logout clears the session
    document.querySelectorAll('header nav a[href="Login-Page.html"]').forEach(function (a) {
      a.addEventListener('click', function () { store.clear(); });
    });

    // Dashboard link for vendors / admins
    var user = store.user;
    var ol = document.querySelector('header nav ol');
    if (ol && user && (user.role === 'ADMIN' || user.role === 'VENDOR')) {
      var li = document.createElement('li');
      li.innerHTML = '<a href="Admin.html"' + (page === 'admin.html' ? ' class="active"' : '') + '> Dashboard </a>';
      ol.insertBefore(li, ol.lastElementChild);
    }
  }

  function setCartCount(n) {
    var link = document.querySelector('header nav a[href="Cart.html"]');
    if (link) link.textContent = n > 0 ? 'Cart (' + n + ')' : 'Cart';
  }

  async function refreshCartCount() {
    if (!store.token || !document.querySelector('header nav a[href="Cart.html"]')) return;
    try { setCartCount((await api('/api/cart')).count); } catch (e) { /* ignore */ }
  }

  // ---------------------------------------------------------------- product cards
  function productCard(p) {
    var out = p.stock <= 0;
    return '<div class="card">' +
      '<div class="card-header"><div class="pic">' +
      '<img referrerpolicy="no-referrer" alt="' + esc(p.brand) + '" src="' + esc(imgSrc(p)) + '">' +
      '</div></div>' +
      '<div class="card-body">' +
      '<h1>' + esc(p.category) + '</h1>' +
      '<h1>Brand : ' + esc(p.brand) + '</h1>' +
      '<h1>Price : ' + money(p.price) + '</h1>' +
      '<h1>Ratings : ' + Number(p.rating).toFixed(1) + '/5</h1>' +
      '<p>' + esc(p.description) + '</p>' +
      '</div>' +
      '<div class="card-footer"><form data-id="' + p.id + '" onsubmit="return false">' +
      '<button type="button" data-act="buy"' + (out ? ' disabled' : '') + '>Buy</button>' +
      '<button type="button" data-act="add"' + (out ? ' disabled' : '') + '>' + (out ? 'Out of stock' : 'Add To Cart') + '</button>' +
      '</form></div></div>';
  }

  function bindCardActions(container) {
    container.addEventListener('click', async function (e) {
      var btn = e.target.closest('button[data-act]');
      if (!btn) return;
      var id = Number(btn.closest('form').dataset.id);
      btn.disabled = true;
      try {
        var cart = await api('/api/cart', { method: 'POST', body: { productId: id, quantity: 1 } });
        if (btn.dataset.act === 'buy') { location.href = 'Cart.html'; return; }
        setCartCount(cart.count);
        toast('Added to cart');
      } catch (err) {
        toast(err.message, true);
      }
      btn.disabled = false;
    });
  }

  // ---------------------------------------------------------------- pages
  async function initHome() {
    var box = $('products-container');
    if (!box) return;
    box.innerHTML = '<p style="padding:20px">Loading products...</p>';
    try {
      var products = await api('/api/products');
      box.innerHTML = products.slice(0, 6).map(productCard).join('') || '<p>No products yet.</p>';
      bindCardActions(box);
    } catch (err) {
      box.innerHTML = '<p style="padding:20px;color:#b71c1c">' + esc(err.message) + '</p>';
    }
  }

  async function initProducts() {
    var box = $('product-list');
    if (!box) return;

    var bar = document.createElement('div');
    bar.className = 'toolbar';
    bar.innerHTML =
      '<input id="search" type="search" placeholder="Search brand or category...">' +
      '<select id="catFilter"><option value="">All categories</option></select>' +
      '<select id="sortBy">' +
      '<option value="">Sort: Featured</option>' +
      '<option value="asc">Price: Low to High</option>' +
      '<option value="desc">Price: High to Low</option>' +
      '<option value="rating">Top rated</option></select>';
    box.parentNode.insertBefore(bar, box);

    var all = [];
    try {
      all = await api('/api/products');
    } catch (err) {
      box.innerHTML = '<p style="padding:20px;color:#b71c1c">' + esc(err.message) + '</p>';
      return;
    }

    var cats = all.map(function (p) { return p.category; })
      .filter(function (c, i, a) { return a.indexOf(c) === i; }).sort();
    $('catFilter').innerHTML = '<option value="">All categories</option>' +
      cats.map(function (c) { return '<option>' + esc(c) + '</option>'; }).join('');

    function render() {
      var q = $('search').value.trim().toLowerCase();
      var cat = $('catFilter').value;
      var sort = $('sortBy').value;
      var list = all.filter(function (p) {
        return (!cat || p.category === cat) &&
          (!q || p.brand.toLowerCase().indexOf(q) >= 0 || p.category.toLowerCase().indexOf(q) >= 0);
      });
      if (sort === 'asc') list.sort(function (a, b) { return a.price - b.price; });
      if (sort === 'desc') list.sort(function (a, b) { return b.price - a.price; });
      if (sort === 'rating') list.sort(function (a, b) { return b.rating - a.rating; });
      box.innerHTML = list.map(productCard).join('') || '<p style="padding:20px">No products match your search.</p>';
    }

    ['search', 'catFilter', 'sortBy'].forEach(function (id) {
      $(id).addEventListener(id === 'search' ? 'input' : 'change', render);
    });
    bindCardActions(box);
    render();
  }

  var QTY = ' class="qty-btn" type="button" ';

  function renderCart(c) {
    var body = $('cart-body');
    if (!c.items.length) {
      body.innerHTML = '<tr><td colspan="8">Your cart is empty. <a href="Products.html">Browse products</a></td></tr>';
    } else {
      body.innerHTML = c.items.map(function (it, i) {
        return '<tr>' +
          '<td>' + (i + 1) + '</td>' +
          '<td class="item-pic"><img referrerpolicy="no-referrer" alt="' + esc(it.brand) + '" src="' + esc(imgSrc(it)) + '"></td>' +
          '<td>' + esc(it.category) + '</td>' +
          '<td>' + esc(it.brand) + '</td>' +
          '<td>' + money(it.price) + '</td>' +
          '<td><button' + QTY + 'data-pid="' + it.productId + '" data-q="' + (it.quantity - 1) + '">&minus;</button> ' +
          '<strong>' + it.quantity + '</strong> ' +
          '<button' + QTY + 'data-pid="' + it.productId + '" data-q="' + (it.quantity + 1) + '">+</button></td>' +
          '<td>' + money(it.subtotal) + '</td>' +
          '<td><a class="remove" href="#" data-remove="' + it.productId + '">Remove</a></td>' +
          '</tr>';
      }).join('');
    }
    $('cart-total').textContent = 'Total : ' + money(c.total);
    setCartCount(c.count);
  }

  async function initCart() {
    if (location.hash === '#order-placed') history.replaceState(null, '', location.pathname);

    var user = store.user;
    if ($('ship-to') && user && user.address) $('ship-to').textContent = 'Delivering to: ' + user.address;

    try {
      renderCart(await api('/api/cart'));
    } catch (err) {
      toast(err.message, true);
    }

    $('cart-body').addEventListener('click', async function (e) {
      var q = e.target.closest('button[data-q]');
      var rm = e.target.closest('a[data-remove]');
      try {
        if (q) {
          renderCart(await api('/api/cart/' + q.dataset.pid, { method: 'PUT', body: { quantity: Number(q.dataset.q) } }));
        } else if (rm) {
          e.preventDefault();
          renderCart(await api('/api/cart/' + rm.dataset.remove, { method: 'DELETE' }));
        }
      } catch (err) {
        toast(err.message, true);
      }
    });

    $('checkout-btn').addEventListener('click', async function (e) {
      e.preventDefault();
      var btn = e.currentTarget;
      if (btn.dataset.busy) return;
      btn.dataset.busy = '1';
      try {
        var order = await api('/api/orders/checkout', {
          method: 'POST',
          body: { shippingAddress: user && user.address ? user.address : '' }
        });
        $('order-msg').textContent = 'Order #' + order.id + ' for ' + money(order.totalAmount) +
          ' has been placed. Thank you for shopping with Think Mart!';
        renderCart({ items: [], total: 0, count: 0 });
        location.hash = 'order-placed';   // opens the pop-up (CSS :target)
      } catch (err) {
        toast(err.message, true);
      }
      delete btn.dataset.busy;
    });
  }

  async function initOrders() {
    var box = $('orders-list');
    try {
      var orders = await api('/api/orders/my');
      if (!orders.length) {
        box.innerHTML = '<p class="page-sub">You have not placed any orders yet. <a href="Products.html">Start shopping</a></p>';
        return;
      }
      box.innerHTML = orders.map(function (o) {
        var rows = o.items.map(function (it) {
          return '<tr><td>' + esc(it.category) + '</td><td>' + esc(it.brand) + '</td><td>' + money(it.unitPrice) +
            '</td><td>' + it.quantity + '</td><td>' + money(it.subtotal) + '</td></tr>';
        }).join('');
        return '<div class="table-wrap order-block">' +
          '<h3>Order #' + o.id + ' &middot; ' + esc(fmtDate(o.createdAt)) + ' &middot; ' +
          '<span class="status-pill">' + esc(o.status) + '</span> &middot; ' + money(o.totalAmount) + '</h3>' +
          '<table><thead><tr><th>Product</th><th>Brand</th><th>Price</th><th>Qty</th><th>Subtotal</th></tr></thead>' +
          '<tbody>' + rows + '</tbody></table>' +
          '<p class="ship">Delivering to: ' + esc(o.shippingAddress) + '</p></div>';
      }).join('');
    } catch (err) {
      box.innerHTML = '<p class="page-sub" style="color:#b71c1c">' + esc(err.message) + '</p>';
    }
  }

  // ---- Dashboard (vendor / admin) ----
  var STATUSES = ['PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  async function initAdmin() {
    var me;
    try {
      me = await api('/api/auth/me');
    } catch (err) { return; }
    if (me.role !== 'ADMIN' && me.role !== 'VENDOR') {
      location.replace('Home-Page.html');
      return;
    }
    $('dash-sub').textContent = me.role === 'ADMIN'
      ? 'Admin: manage all products, orders and messages'
      : 'Vendor: manage your own products';

    var products = [];

    function resetForm() {
      $('productForm').reset();
      $('pid').value = '';
      $('pstock').value = 10;
      $('formTitle').textContent = 'Add a product';
      $('cancelEdit').style.display = 'none';
      setMsg('');
    }

    function renderProducts() {
      $('prod-body').innerHTML = products.map(function (p, i) {
        return '<tr><td>' + (i + 1) + '</td><td>' + esc(p.category) + '</td><td>' + esc(p.brand) + '</td><td>' +
          money(p.price) + '</td><td>' + p.stock + '</td><td>' +
          '<button class="small-btn" data-edit="' + p.id + '">Edit</button>' +
          '<button class="small-btn danger" data-del="' + p.id + '">Delete</button></td></tr>';
      }).join('') || '<tr><td colspan="6">No products yet.</td></tr>';
    }

    async function loadProducts() {
      products = await api('/api/products/mine');
      renderProducts();
    }

    $('prod-body').addEventListener('click', async function (e) {
      var ed = e.target.closest('button[data-edit]');
      var del = e.target.closest('button[data-del]');
      if (ed) {
        var p = products.filter(function (x) { return x.id === Number(ed.dataset.edit); })[0];
        if (!p) return;
        $('pid').value = p.id;
        $('pcategory').value = p.category;
        $('pbrand').value = p.brand;
        $('pprice').value = p.price;
        $('prating').value = p.rating;
        $('pstock').value = p.stock;
        $('pimage').value = p.imageUrl || '';
        $('pdesc').value = p.description || '';
        $('formTitle').textContent = 'Edit product #' + p.id;
        $('cancelEdit').style.display = 'inline-block';
        $('productForm').scrollIntoView({ behavior: 'smooth' });
      } else if (del) {
        if (!confirm('Delete this product?')) return;
        try {
          await api('/api/products/' + del.dataset.del, { method: 'DELETE' });
          toast('Product deleted');
          await loadProducts();
        } catch (err) { toast(err.message, true); }
      }
    });

    $('cancelEdit').addEventListener('click', resetForm);

    $('productForm').addEventListener('submit', async function (e) {
      e.preventDefault();
      var id = $('pid').value;
      var payload = {
        category: $('pcategory').value.trim(),
        brand: $('pbrand').value.trim(),
        price: Number($('pprice').value),
        rating: $('prating').value === '' ? 0 : Number($('prating').value),
        stock: parseInt($('pstock').value, 10),
        imageUrl: $('pimage').value.trim(),
        description: $('pdesc').value.trim()
      };
      try {
        await api(id ? '/api/products/' + id : '/api/products', { method: id ? 'PUT' : 'POST', body: payload });
        toast(id ? 'Product updated' : 'Product added');
        resetForm();
        await loadProducts();
      } catch (err) {
        setMsg(err.message);
      }
    });

    try { await loadProducts(); } catch (err) { toast(err.message, true); }

    if (me.role === 'ADMIN') {
      $('admin-only').style.display = 'block';
      try {
        var orders = await api('/api/admin/orders');
        $('admin-orders').innerHTML = orders.map(function (o) {
          var items = o.items.map(function (it) { return esc(it.brand) + ' x' + it.quantity; }).join('<br>');
          var opts = STATUSES.map(function (s) {
            return '<option' + (s === o.status ? ' selected' : '') + '>' + s + '</option>';
          }).join('');
          return '<tr><td>#' + o.id + '</td><td>' + esc(o.customerName) + '<br><small>' + esc(o.customerEmail) +
            '</small></td><td>' + esc(fmtDate(o.createdAt)) + '</td><td>' + items + '</td><td>' + money(o.totalAmount) +
            '</td><td><select data-order="' + o.id + '">' + opts + '</select></td></tr>';
        }).join('') || '<tr><td colspan="6">No orders yet.</td></tr>';

        var msgs = await api('/api/admin/messages');
        $('admin-messages').innerHTML = msgs.map(function (m) {
          return '<tr><td>' + esc(fmtDate(m.createdAt)) + '</td><td>' + esc(m.name) + '</td><td>' + esc(m.email) +
            '</td><td>' + esc(m.subject) + '</td><td style="text-align:left">' + esc(m.message) + '</td></tr>';
        }).join('') || '<tr><td colspan="5">No messages yet.</td></tr>';
      } catch (err) { toast(err.message, true); }

      $('admin-orders').addEventListener('change', async function (e) {
        var sel = e.target.closest('select[data-order]');
        if (!sel) return;
        try {
          await api('/api/admin/orders/' + sel.dataset.order + '/status', { method: 'PUT', body: { status: sel.value } });
          toast('Order #' + sel.dataset.order + ' is now ' + sel.value);
        } catch (err) {
          toast(err.message, true);
          setTimeout(function () { location.reload(); }, 1200);
        }
      });
    }
  }

  // ---- Contact ----
  function initContact() {
    var form = $('contactForm');
    var user = store.user;
    if (user) {
      $('cname').value = (user.firstName + ' ' + user.lastName).trim();
      $('cemail').value = user.email;
    }
    form.addEventListener('submit', async function (e) {
      e.preventDefault();
      try {
        var r = await api('/api/contact', {
          method: 'POST',
          body: {
            name: $('cname').value.trim(),
            email: $('cemail').value.trim(),
            subject: $('csubject').value,
            message: $('cmsg').value.trim()
          }
        });
        setMsg(r.message, true);
        toast(r.message);
        $('cmsg').value = '';
      } catch (err) {
        setMsg(err.message);
        $('formMsg').style.color = '#b71c1c';
      }
    });
  }

  // ---- Login ----
  function initLogin() {
    $('loginForm').addEventListener('submit', async function (e) {
      e.preventDefault();
      setMsg('');
      try {
        var res = await api('/api/auth/login', {
          method: 'POST',
          body: { email: $('email').value.trim(), password: $('password').value }
        });
        store.save(res.token, res.user);
        location.href = 'Home-Page.html';
      } catch (err) {
        setMsg(err.message);
      }
    });
  }

  // ---- Register ----
  function initRegister() {
    $('registerForm').addEventListener('submit', async function (e) {
      e.preventDefault();
      setMsg('');
      if ($('psw').value !== $('cpsw').value) {
        setMsg('Passwords do not match');
        return;
      }
      var gender = document.querySelector('input[name="gender"]:checked');
      var role = document.querySelector('input[name="role"]:checked');

      var fd = new FormData();
      fd.append('firstName', $('fname').value.trim());
      fd.append('lastName', $('lname').value.trim());
      fd.append('email', $('email').value.trim());
      fd.append('phone', $('phn').value.trim());
      fd.append('password', $('psw').value);
      fd.append('dob', $('dob').value);
      fd.append('gender', gender ? gender.value : '');
      fd.append('state', $('state').value);
      fd.append('address', $('address').value.trim());
      fd.append('role', role ? role.value : '');
      var pic = $('pic').files[0];
      if (pic) fd.append('pic', pic);

      try {
        await api('/api/auth/register', { method: 'POST', body: fd });
        alert('Account created! Please log in.');
        location.href = 'Login-Page.html';
      } catch (err) {
        setMsg(err.message);
      }
    });
  }

  // ---------------------------------------------------------------- start
  if (!isPublicPage) {
    initNav();
    if (page !== 'cart.html') refreshCartCount();
  }

  switch (page) {
    case 'home-page.html': initHome(); break;
    case 'products.html': initProducts(); break;
    case 'cart.html': initCart(); break;
    case 'orders.html': initOrders(); break;
    case 'admin.html': initAdmin(); break;
    case 'contact.html': initContact(); break;
    case 'login-page.html': initLogin(); break;
    case 'register-page.html': initRegister(); break;
    default: break;
  }
})();
