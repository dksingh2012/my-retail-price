-- MY RETAIL PRICE D1 schema
CREATE TABLE IF NOT EXISTS products (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  pack TEXT,
  brand TEXT,
  unit_value REAL,
  unit TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS offers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  product_id INTEGER NOT NULL,
  retailer TEXT NOT NULL,
  price REAL NOT NULL,
  mrp REAL,
  pincode TEXT NOT NULL,
  available INTEGER NOT NULL DEFAULT 1,
  source TEXT NOT NULL DEFAULT 'demo',
  data_status TEXT NOT NULL DEFAULT 'demo',
  product_url TEXT,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(product_id) REFERENCES products(id)
);

CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_offers_product ON offers(product_id);
CREATE INDEX IF NOT EXISTS idx_offers_pincode ON offers(pincode);
