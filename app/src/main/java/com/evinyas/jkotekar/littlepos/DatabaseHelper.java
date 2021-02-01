package com.evinyas.jkotekar.littlepos;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.evinyas.jkotekar.littlepos.model.Cost;
import com.evinyas.jkotekar.littlepos.model.CostData;
import com.evinyas.jkotekar.littlepos.model.Customer;
import com.evinyas.jkotekar.littlepos.model.CustomerProduct;
import com.evinyas.jkotekar.littlepos.model.Product;
import com.evinyas.jkotekar.littlepos.model.salesData;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    static final String DATABASE_NAME = "pepsiSale";
    private static final int DATABASE_VERSION = 4;

    // Table Names
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_CUSTOMER_PRODUCTS = "customerProducts";
    private static final String TABLE_PRICE_CUSTOMER = "customerPrice";
    private static final String TABLE_SALES = "SALES";
    private static final String TABLE_COST_ITEM = "cost";
    private static final String TABLE_COSTDATA = "costdata";

    // Customer Table Columns
    private static final String KEY_CUSTOMER_ID = "cid";
    private static final String KEY_CUSTOMER_NAME = "customerName";
    private static final String KEY_CUSTOMER_PHONE = "customerPhone";

    // Products Table Columns
    private static final String KEY_PRODUCT_ID = "pid";
    private static final String KEY_PRODUCT_NAME = "productName";
    private static final String KEY_PRODUCT_DPRICE = "productDPrice";

    // Customer products Columns
    private static final String KEY_CUSPRD_ID = "cpid";
    private static final String KEY_CUSPRD_PRDCT_ID = "productID";
    private static final String KEY_CUSPRD_CUST_ID = "customerID";
    private static final String KEY_CUSPRD_PRICE = "customerPrice";

    // Customer cost Columns
    private static final String KEY_PRCCUS_ID = "pcid";
    private static final String KEY_PRCCUS_PRDCT_ID = "productID";
    private static final String KEY_PRCCUS_CUST_ID = "customerID";
    private static final String KEY_PRCCUS_PRICE = "customerPrice";

    // Sales Columns
    private static final String KEY_SALES_SLNO = "slno";
    private static final String KEY_SALES_DATE = "date";
    private static final String KEY_SALES_CUSTID = "customerID";
    private static final String KEY_SALES_PRODID = "productID";
    private static final String KEY_SALES_QUANTITY = "quantity";
    private static final String KEY_SALES_PRICE = "salesPrice";
    private static final String KEY_SALES_AMOUNT = "amount";
    private static final String KEY_SALES_RECEIVED = "received";
    private static final String KEY_SALES_COMMENTS = "comments";

    // Cost Items Table Columns
    private static final String KEY_COST_ID = "cid";
    private static final String KEY_COST_NAME = "costName";
    private static final String KEY_COST_COST = "costPrice";
    private static final String KEY_COST_DATA1 = "costData1";
    private static final String KEY_COST_DATA2 = "costData2";

    // CostEntry Columns
    private static final String KEY_COSTDATA_SLNO = "slno";
    private static final String KEY_COSTDATA_DATE = "date";
    private static final String KEY_COSTDATA_COSTID = "costId";
    private static final String KEY_COSTDATA_QUANTITY = "quantity";
    private static final String KEY_COSTDATA_COST = "cost";
    private static final String KEY_COSTDATA_AMOUNT = "amount";
    private static final String KEY_COSTDATA_COMMENTS = "comments";
    private static final String KEY_COSTDATA_Data1 = "Data1";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_CUSTOMER_TABLE = "CREATE TABLE " + TABLE_CUSTOMERS +
                "(" +
                KEY_CUSTOMER_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_CUSTOMER_NAME + " TEXT," +
                KEY_CUSTOMER_PHONE + " TEXT" +
                ")";

        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS +
                "(" +
                KEY_PRODUCT_ID + " INTEGER PRIMARY KEY," +
                KEY_PRODUCT_NAME + " TEXT," +
                KEY_PRODUCT_DPRICE + " TEXT NOT NULL DEFAULT '0'" +
                ")";

        String CREATE_CUSTOMER_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_CUSTOMER_PRODUCTS +
                "(" +
                KEY_CUSPRD_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_CUSPRD_CUST_ID + " INTEGER," +
                KEY_CUSPRD_PRDCT_ID + " INTEGER," +
                KEY_CUSPRD_PRICE + " TEXT NOT NULL DEFAULT '0'," +
                "FOREIGN KEY(" + KEY_CUSPRD_CUST_ID + ") REFERENCES " + TABLE_CUSTOMERS + "(" + KEY_CUSTOMER_ID + ")," +
                "FOREIGN KEY(" + KEY_CUSPRD_PRDCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_PRODUCT_ID + ")," +
                "UNIQUE (" + KEY_CUSPRD_CUST_ID + ", " + KEY_CUSPRD_PRDCT_ID + ")" +
                ")";

        String CREATE_PRICE_CUSTOMER_TABLE = "CREATE TABLE " + TABLE_PRICE_CUSTOMER +
                "(" +
                KEY_PRCCUS_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_PRCCUS_CUST_ID + " INTEGER," +
                KEY_PRCCUS_PRDCT_ID + " INTEGER," +
                KEY_PRCCUS_PRICE + " TEXT NOT NULL DEFAULT '0'," +
                "FOREIGN KEY(" + KEY_CUSPRD_CUST_ID + ") REFERENCES " + TABLE_CUSTOMERS + "(" + KEY_CUSTOMER_ID + ")," +
                "FOREIGN KEY(" + KEY_CUSPRD_PRDCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_PRODUCT_ID + ")," +
                "UNIQUE (" + KEY_PRCCUS_CUST_ID + ", " + KEY_PRCCUS_PRDCT_ID + ")" +
                ")";

        String CREATE_SALES_TABLE = "CREATE TABLE " + TABLE_SALES +
                "(" +
                KEY_SALES_SLNO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_SALES_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                KEY_SALES_CUSTID + " INTEGER," +
                KEY_SALES_PRODID + " INTEGER DEFAULT NULL," +
                KEY_SALES_QUANTITY + " INTEGER DEFAULT 0," +
                KEY_SALES_PRICE + " REAL DEFAULT 0," +
                KEY_SALES_AMOUNT + " REAL DEFAULT 0," +
                KEY_SALES_RECEIVED + " REAL DEFAULT 0," +
                KEY_SALES_COMMENTS + " TEXT DEFAULT ''," +
                "FOREIGN KEY(" + KEY_SALES_CUSTID + ") REFERENCES " + TABLE_CUSTOMERS + "(" + KEY_CUSTOMER_ID + ")," +
                "FOREIGN KEY(" + KEY_SALES_PRODID + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_PRODUCT_ID + ")" +
                ")";

        String CREATE_COST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COST_ITEM +
                "(" +
                KEY_COST_ID + " INTEGER PRIMARY KEY," +
                KEY_COST_NAME + " TEXT," +
                KEY_COST_COST + " TEXT NOT NULL DEFAULT '0'," +
                KEY_COST_DATA1 + " TEXT," +
                KEY_COST_DATA2 + " TEXT" +
                ")";

        String CREATE_COSTDATA_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COSTDATA +
                "(" +
                KEY_COSTDATA_SLNO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_COSTDATA_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                KEY_COSTDATA_COSTID + " INTEGER DEFAULT NULL," +
                KEY_COSTDATA_QUANTITY + " INTEGER DEFAULT 0," +
                KEY_COSTDATA_COST + " REAL DEFAULT 0," +
                KEY_COSTDATA_AMOUNT + " REAL DEFAULT 0," +
                KEY_COSTDATA_Data1 + " TEXT," +
                KEY_COSTDATA_COMMENTS + " TEXT DEFAULT ''," +
                "FOREIGN KEY(" + KEY_COSTDATA_COSTID + ") REFERENCES " + TABLE_COST_ITEM + "(" + KEY_COST_ID + ")" +
                ")";

        db.execSQL(CREATE_CUSTOMER_TABLE);
        db.execSQL(CREATE_PRODUCTS_TABLE);
        db.execSQL(CREATE_CUSTOMER_PRODUCTS_TABLE);
        db.execSQL(CREATE_PRICE_CUSTOMER_TABLE);
        db.execSQL(CREATE_SALES_TABLE);
        db.execSQL(CREATE_COST_TABLE);
        db.execSQL(CREATE_COSTDATA_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase database) {
        super.onOpen(database);
        if (Build.VERSION.SDK_INT >= 28) {
            database.disableWriteAheadLogging();
        }
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
/*            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER_PRODUCTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE_CUSTOMER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES);*/
            String CREATE_COST_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COST_ITEM +
                    "(" +
                    KEY_COST_ID + " INTEGER PRIMARY KEY," +
                    KEY_COST_NAME + " TEXT," +
                    KEY_COST_COST + " TEXT NOT NULL DEFAULT '0'," +
                    KEY_COST_DATA1 + " TEXT," +
                    KEY_COST_DATA2 + " TEXT" +
                    ")";

            String CREATE_COSTDATA_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COSTDATA +
                    "(" +
                    KEY_COSTDATA_SLNO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    KEY_COSTDATA_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    KEY_COSTDATA_COSTID + " INTEGER DEFAULT NULL," +
                    KEY_COSTDATA_QUANTITY + " INTEGER DEFAULT 0," +
                    KEY_COSTDATA_COST + " REAL DEFAULT 0," +
                    KEY_COSTDATA_AMOUNT + " REAL DEFAULT 0," +
                    KEY_COSTDATA_Data1 + " TEXT," +
                    KEY_COSTDATA_COMMENTS + " TEXT DEFAULT ''," +
                    "FOREIGN KEY(" + KEY_COSTDATA_COSTID + ") REFERENCES " + TABLE_COST_ITEM + "(" + KEY_COST_ID + ")" +
                    ")";

            db.execSQL(CREATE_COST_TABLE);
            db.execSQL(CREATE_COSTDATA_TABLE);
            //onCreate(db);
        }
    }

    // Insert a post into the database
    long addCustomer(Customer customer) {
        long returnid = 0;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(KEY_CUSTOMER_NAME, customer.getCustomerName());
            values.put(KEY_CUSTOMER_PHONE, customer.getCustomerPhone());

            returnid = db.insertOrThrow(TABLE_CUSTOMERS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Customer to database");
        } finally {
            db.endTransaction();
            Log.d(TAG, "Record Added");
        }
        return returnid;
    }

    // Get all customers in the database
    List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String CUSTOMERS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_CUSTOMERS
                );

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(CUSTOMERS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Customer newCustomer = new Customer();
                    newCustomer.setCustomerName(cursor.getString(cursor.getColumnIndex(KEY_CUSTOMER_NAME)));
                    newCustomer.setCustomerPhone(cursor.getString(cursor.getColumnIndex(KEY_CUSTOMER_PHONE)));
                    newCustomer.setId(cursor.getInt(cursor.getColumnIndex(KEY_CUSTOMER_ID)));
                    customers.add(newCustomer);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Customers from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return customers;
    }

    int updateCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;

        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOMER_NAME, customer.getCustomerName());
        values.put(KEY_CUSTOMER_PHONE, customer.getCustomerPhone());

        try {
            retID = db.update(TABLE_CUSTOMERS, values, KEY_CUSTOMER_ID + " = ?",
                    new String[]{String.valueOf(customer.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }


        return retID;
    }

    int deleteCustomer(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;

        try {

            retID = db.delete(TABLE_CUSTOMERS, KEY_CUSTOMER_ID + " = ?",
                    new String[]{String.valueOf(id)});

        } catch (Exception e) {
            e.printStackTrace();
        }

        return retID;
    }

    // Insert a costItem into the database
    Long addProduct(Product product) {
        long returnid = 0;
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(KEY_PRODUCT_NAME, product.getProductName());
            values.put(KEY_PRODUCT_DPRICE, product.getProductDPrice());

            returnid = db.insertOrThrow(TABLE_PRODUCTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Customer to database");
        } finally {
            db.endTransaction();
        }
        return returnid;
    }

    Long addCostItem(Cost cost) {
        Log.d(TAG, "***** ADD COST *****");
        long returnid = 0;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_COST_NAME, cost.getCostName());
            values.put(KEY_COST_COST, cost.getCost());
            returnid = db.insertOrThrow(TABLE_COST_ITEM, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Cost to database");
        } finally {
            db.endTransaction();
        }
        return returnid;
    }

    List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        String PRODUCTS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_PRODUCTS
                );
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(PRODUCTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Product newProduct = new Product();
                    newProduct.setProductName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                    newProduct.setProductDPrice(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_DPRICE)));
                    newProduct.setId(cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_ID)));
                    products.add(newProduct);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Products from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return products;
    }

    List<Cost> getAllCostItem() {
        List<Cost> costs = new ArrayList<>();

        String COST_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_COST_ITEM);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(COST_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Cost newCost = new Cost();
                    newCost.setCostName(cursor.getString(cursor.getColumnIndex(KEY_COST_NAME)));
                    newCost.setCost(cursor.getString(cursor.getColumnIndex(KEY_COST_COST)));
                    newCost.setId(cursor.getInt(cursor.getColumnIndex(KEY_COST_ID)));
                    costs.add(newCost);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Cost from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return costs;
    }

    Double getProductPricebyID(int ID) {
        Double productPrice = null;

        String PRODUCTS_SELECT_QUERY =
                String.format("SELECT %s FROM %s where %s = %s",
                        KEY_PRODUCT_DPRICE,
                        TABLE_PRODUCTS,
                        KEY_PRODUCT_ID,
                        ID
                );
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(PRODUCTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {

                    try {
                        productPrice = Double.parseDouble(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_DPRICE)));
                    } catch (NumberFormatException e) {
                        productPrice = 0.00;
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Products Price from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return productPrice;
    }

    ///update costItem
    int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;

        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_NAME, product.getProductName());
        values.put(KEY_PRODUCT_DPRICE, product.getProductDPrice());

        try {
            retID = db.update(TABLE_PRODUCTS, values, KEY_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(product.getId())});

        } catch (Exception e) {
            e.printStackTrace();
        }


        return retID;
    }

    ///update Cost Item
    int updateCostItem(Cost cost) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;

        ContentValues values = new ContentValues();
        values.put(KEY_COST_NAME, cost.getCostName());
        values.put(KEY_COST_COST, cost.getCost());

        try {
            retID = db.update(TABLE_COST_ITEM, values, KEY_COST_ID + " = ?",
                    new String[]{String.valueOf(cost.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retID;
    }

    // Delete Customer
    int deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;
        try {

            retID = db.delete(TABLE_PRODUCTS, KEY_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(id)});

        } catch (Exception e) {
            e.printStackTrace();
        }

        return retID;
    }

    // Delete Customer
    int deleteCost(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;
        try {

            retID = db.delete(TABLE_COST_ITEM, KEY_COST_ID + " = ?",
                    new String[]{String.valueOf(id)});

        } catch (Exception e) {
            e.printStackTrace();
        }

        return retID;
    }

    void addCustomerProducts(CustomerProduct customerProduct) {

        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_CUSPRD_CUST_ID, customerProduct.getCustomerID());
            values.put(KEY_CUSPRD_PRDCT_ID, customerProduct.getProductID());
            values.put(KEY_CUSPRD_PRICE, customerProduct.getCustomerPrice());

            db.insertWithOnConflict(TABLE_CUSTOMER_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Customer Product to database : " + e);
        } finally {
            db.endTransaction();
            Log.d(TAG, "Record Added");
        }
    }

    List<CustomerProduct> getAllcustomerProduct(int ID) {
        List<CustomerProduct> customerproducts = new ArrayList<>();
        String CUSTOMERPRODUCTS_SELECT_QUERY;
        if (ID == 0) {
            CUSTOMERPRODUCTS_SELECT_QUERY =
                    String.format("SELECT * FROM %s",
                            TABLE_CUSTOMER_PRODUCTS
                    );


        } else {
            CUSTOMERPRODUCTS_SELECT_QUERY =
                    String.format("SELECT * FROM %s where %s=%s",
                            TABLE_CUSTOMER_PRODUCTS,
                            KEY_CUSPRD_CUST_ID,
                            ID
                    );
        }

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(CUSTOMERPRODUCTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    CustomerProduct cp = new CustomerProduct();

                    cp.setCustomerID(cursor.getInt(cursor.getColumnIndex(KEY_CUSPRD_CUST_ID)));
                    cp.setProductID(cursor.getInt(cursor.getColumnIndex(KEY_CUSPRD_PRDCT_ID)));
                    cp.setId(cursor.getInt(cursor.getColumnIndex(KEY_CUSPRD_ID)));
                    cp.setCustomerPrice(cursor.getString(cursor.getColumnIndex(KEY_CUSPRD_PRICE)));

                    customerproducts.add(cp);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Customer Products from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return customerproducts;
    }

    List<CustomerProduct> getAllcustomerProductName(int custID) {
        List<CustomerProduct> customerproducts = new ArrayList<>();

        String CUSTOMERPRODUCTS_ALL_SELECT_QUERY;
        if (custID == 0)
            CUSTOMERPRODUCTS_ALL_SELECT_QUERY = String.format("SELECT %s.%s AS %s,%s.%s,%s.%s AS %s,%s.%s,%s.%s FROM %s INNER JOIN %s ON %s.%s=%s.%s INNER JOIN %s ON %s.%s=%s.%s ORDER BY %s",
                    TABLE_CUSTOMERS, KEY_CUSTOMER_ID,
                    //as cusotmer id
                    KEY_CUSTOMER_ID,
                    TABLE_CUSTOMERS, KEY_CUSTOMER_NAME,
                    TABLE_PRODUCTS, KEY_PRODUCT_ID,
                    //as costItem ID
                    KEY_PRODUCT_ID,
                    TABLE_PRODUCTS, KEY_PRODUCT_NAME,
                    TABLE_CUSTOMER_PRODUCTS, KEY_CUSPRD_PRICE,
                    //from
                    TABLE_CUSTOMERS,
                    TABLE_CUSTOMER_PRODUCTS,
                    TABLE_CUSTOMERS, KEY_CUSTOMER_ID,
                    TABLE_CUSTOMER_PRODUCTS, KEY_CUSPRD_CUST_ID,
                    TABLE_PRODUCTS,
                    TABLE_PRODUCTS, KEY_PRODUCT_ID,
                    TABLE_CUSTOMER_PRODUCTS, KEY_CUSPRD_PRDCT_ID,
                    KEY_CUSTOMER_NAME
            );
        else
            CUSTOMERPRODUCTS_ALL_SELECT_QUERY = String.format("SELECT %s.%s AS %s,%s.%s,%s.%s AS %s,%s.%s,%s.%s FROM %s INNER JOIN %s ON %s.%s=%s.%s INNER JOIN %s ON %s.%s=%s.%s where %s.%s=%s",
                    TABLE_CUSTOMERS, KEY_CUSTOMER_ID,
                    KEY_CUSTOMER_ID,
                    TABLE_CUSTOMERS, KEY_CUSTOMER_NAME,
                    TABLE_PRODUCTS, KEY_PRODUCT_ID,
                    KEY_PRODUCT_ID,
                    TABLE_PRODUCTS, KEY_PRODUCT_NAME,
                    TABLE_CUSTOMER_PRODUCTS, KEY_CUSPRD_PRICE,
                    TABLE_CUSTOMERS,
                    TABLE_CUSTOMER_PRODUCTS,
                    TABLE_CUSTOMERS, KEY_CUSTOMER_ID,
                    TABLE_CUSTOMER_PRODUCTS, KEY_CUSPRD_CUST_ID,
                    TABLE_PRODUCTS,
                    TABLE_PRODUCTS, KEY_PRODUCT_ID,
                    TABLE_CUSTOMER_PRODUCTS, KEY_CUSPRD_PRDCT_ID,
                    TABLE_CUSTOMER_PRODUCTS, KEY_CUSPRD_CUST_ID,
                    custID
            );

        System.out.println("Query " + CUSTOMERPRODUCTS_ALL_SELECT_QUERY);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(CUSTOMERPRODUCTS_ALL_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    CustomerProduct cp = new CustomerProduct();
                    cp.setCustomerID(cursor.getInt(cursor.getColumnIndex(KEY_CUSTOMER_ID)));
                    cp.setCustomerName(cursor.getString(cursor.getColumnIndex(KEY_CUSTOMER_NAME)));
                    cp.setProductID(cursor.getInt(cursor.getColumnIndex(KEY_PRODUCT_ID)));
                    cp.setProductName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                    cp.setCustomerPrice(cursor.getString(cursor.getColumnIndex(KEY_CUSPRD_PRICE)));

                    customerproducts.add(cp);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Customer Products from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return customerproducts;
    }

    void deleteCustomerProduct(String CID, String PID) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(TABLE_CUSTOMER_PRODUCTS, KEY_CUSPRD_CUST_ID + " = ? AND " + KEY_CUSPRD_PRDCT_ID + " = ?", new String[]{CID, PID});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while deleting Customer Product from database : " + e);
        } finally {
            db.endTransaction();
            Log.d(TAG, "Record deleted");
        }
    }

    void addCustomerPrice(CustomerProduct customerProduct) {

        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_CUSPRD_CUST_ID, customerProduct.getCustomerID());
            values.put(KEY_CUSPRD_PRDCT_ID, customerProduct.getProductID());
            values.put(KEY_CUSPRD_PRICE, customerProduct.getCustomerPrice());

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertWithOnConflict(TABLE_CUSTOMER_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Customer Price to database" + e);
        } finally {
            db.endTransaction();
            Log.d(TAG, "Customer Price Record Added");
        }
    }

    long addSales(salesData sales) {
        long result = 0;
        // Create and/or open the database for writing\=
        SQLiteDatabase db = getWritableDatabase();

        // consistency of the database.
        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();

            if (sales.getDate() != null)
                values.put(KEY_SALES_DATE, sales.getDate());
            values.put(KEY_SALES_CUSTID, sales.getCustID());
            values.put(KEY_SALES_PRODID, sales.getProdID());
            values.put(KEY_SALES_QUANTITY, sales.getQuantity());
            values.put(KEY_SALES_PRICE, sales.getPrice());
            if (sales.getAmount() != null)
                values.put(KEY_SALES_AMOUNT, sales.getAmount());
            if (sales.getReceived() != null)
                values.put(KEY_SALES_RECEIVED, sales.getReceived());
            if (sales.getComments() != null)
                values.put(KEY_SALES_COMMENTS, sales.getComments());

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            result = db.insertWithOnConflict(TABLE_SALES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Sales to database" + e);
        } finally {
            db.endTransaction();
            Log.d(TAG, "Sales Record Added");
        }
        return result;
    }

    long addCostData(CostData sales) {
        long result = 0;
        // Create and/or open the database for writing\=
        SQLiteDatabase db = getWritableDatabase();

        // consistency of the database.
        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();

            if (sales.getDate() != null)
                values.put(KEY_COSTDATA_DATE, sales.getDate());
            values.put(KEY_COSTDATA_COSTID, sales.getCostId());
            values.put(KEY_COSTDATA_QUANTITY, sales.getQuantity());
            values.put(KEY_COSTDATA_COST, sales.getCost());
            if (sales.getAmount() != null)
                values.put(KEY_COSTDATA_AMOUNT, sales.getAmount());
            if (sales.getComments() != null)
                values.put(KEY_COSTDATA_COMMENTS, sales.getComments());

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            result = db.insertWithOnConflict(TABLE_COSTDATA, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Cost data to database" + e);
        } finally {
            db.endTransaction();
            Log.d(TAG, "Costs Record Added");
        }
        return result;
    }

    List<salesData> getSalesReportbyDate(int customerID, String from, String to) {
        List<salesData> sales = new ArrayList<>();
        String SALES_SELECT_QUERY;
        if (customerID > 0) {
            SALES_SELECT_QUERY = String.format("select %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s from %s left join %s on %s.%s = %s.%s left join %s on %s.%s = %s.%s where date(%s) between '%s' and '%s' and %s=%s order by date(%s)",
                    KEY_SALES_SLNO,
                    KEY_SALES_DATE,
                    KEY_CUSTOMER_NAME,
                    KEY_SALES_CUSTID,
                    KEY_PRODUCT_NAME,
                    KEY_SALES_PRODID,
                    KEY_SALES_QUANTITY,
                    KEY_SALES_PRICE,
                    KEY_SALES_AMOUNT,
                    KEY_SALES_RECEIVED,
                    KEY_SALES_COMMENTS,
                    //from
                    TABLE_SALES,
                    TABLE_PRODUCTS,
                    //on
                    TABLE_SALES, KEY_SALES_PRODID,
                    TABLE_PRODUCTS, KEY_PRODUCT_ID,
                    //left join
                    TABLE_CUSTOMERS,
                    TABLE_SALES, KEY_SALES_CUSTID,
                    TABLE_CUSTOMERS, KEY_CUSTOMER_ID,
                    KEY_SALES_DATE,
                    from,
                    to,
                    KEY_SALES_CUSTID,
                    customerID,
                    KEY_SALES_DATE
            );
        } else {
            SALES_SELECT_QUERY = String.format("select %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s from %s left join %s on %s.%s = %s.%s left join %s on %s.%s = %s.%s where date(%s) between '%s' and '%s' order by date(%s)",
                    KEY_SALES_SLNO,
                    KEY_SALES_DATE,
                    KEY_CUSTOMER_NAME,
                    KEY_SALES_CUSTID,
                    KEY_PRODUCT_NAME,
                    KEY_SALES_PRODID,
                    KEY_SALES_QUANTITY,
                    KEY_SALES_PRICE,
                    KEY_SALES_AMOUNT,
                    KEY_SALES_RECEIVED,
                    KEY_SALES_COMMENTS,
                    //from
                    TABLE_SALES,
                    TABLE_PRODUCTS,
                    //on
                    TABLE_SALES, KEY_SALES_PRODID,
                    TABLE_PRODUCTS, KEY_PRODUCT_ID,
                    //left join
                    TABLE_CUSTOMERS,
                    TABLE_SALES, KEY_SALES_CUSTID,
                    TABLE_CUSTOMERS, KEY_CUSTOMER_ID,
                    KEY_SALES_DATE,
                    from,
                    to,
                    KEY_SALES_DATE
            );
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SALES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    salesData sd = new salesData();
                    sd.setSlno(cursor.getString(cursor.getColumnIndex(KEY_SALES_SLNO)));
                    sd.setDate(cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE)));
                    sd.setCustomerName(cursor.getString(cursor.getColumnIndex(KEY_CUSTOMER_NAME)));
                    sd.setProductName(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                    sd.setQuantity(cursor.getString(cursor.getColumnIndex(KEY_SALES_QUANTITY)));
                    sd.setPrice(cursor.getString(cursor.getColumnIndex(KEY_SALES_PRICE)));
                    sd.setAmount(cursor.getString(cursor.getColumnIndex(KEY_SALES_AMOUNT)));
                    sd.setReceived(cursor.getString(cursor.getColumnIndex(KEY_SALES_RECEIVED)));
                    sd.setComments(cursor.getString(cursor.getColumnIndex(KEY_SALES_COMMENTS)));
                    sales.add(sd);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Customer Sales Report from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return sales;
    }

    List<CostData> getCostReportbyDate(String from, String to) {
        List<CostData> costData = new ArrayList<>();
        String COSTDATA_SELECT_QUERY;
        COSTDATA_SELECT_QUERY = String.format("select %s,%s,%s,%s,%s,%s,%s,%s from %s left join %s on %s.%s = %s.%s where date(%s) between '%s' and '%s' order by date(%s)",
                KEY_COSTDATA_SLNO,
                KEY_COSTDATA_DATE,
                KEY_COSTDATA_COSTID,
                KEY_COSTDATA_QUANTITY,
                KEY_COSTDATA_COST,
                KEY_COSTDATA_AMOUNT,
                KEY_COSTDATA_COMMENTS,
                KEY_COST_NAME,
                //from
                TABLE_COSTDATA,
                //left join
                TABLE_COST_ITEM,
                //on
                TABLE_COSTDATA, KEY_COSTDATA_COSTID,
                TABLE_COST_ITEM, KEY_COST_ID,
                //where
                KEY_COSTDATA_DATE,
                from,
                to,
                KEY_COSTDATA_DATE
        );
        Log.d(TAG, COSTDATA_SELECT_QUERY);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(COSTDATA_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    CostData sd = new CostData();
                    sd.setSlno(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_SLNO)));
                    sd.setDate(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_DATE)));
                    sd.setCostId(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_COSTID)));
                    sd.setQuantity(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_QUANTITY)));
                    sd.setCost(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_COST)));
                    sd.setAmount(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_AMOUNT)));
                    sd.setComments(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_COMMENTS)));
                    sd.setCostName(cursor.getString(cursor.getColumnIndex(KEY_COST_NAME)));
                    costData.add(sd);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Cost Data from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return costData;
    }

    List<String> getSalesSumbyCustomer(int custID, String fromDate, String toDate) {
        List<String> data = new ArrayList<>();

        String SALESDATA_SUM_SALES_QUERY;
        if (custID == 0 && fromDate.length() == 0)
            SALESDATA_SUM_SALES_QUERY = String.format("select sum(%s) as %s,sum(%s) as %s,sum(%s)-sum(%s) as 'balance' from %s",
                    KEY_SALES_AMOUNT,
                    KEY_SALES_AMOUNT,
                    KEY_SALES_RECEIVED,
                    KEY_SALES_RECEIVED,
                    KEY_SALES_AMOUNT,
                    KEY_SALES_RECEIVED,
                    TABLE_SALES
            );
        else if (custID != 0 && fromDate.length() == 0 && toDate.length() == 0) {
            SALESDATA_SUM_SALES_QUERY = String.format("select sum(%s) as %s,sum(%s) as %s from %s where %s=%s",
                    KEY_SALES_AMOUNT,
                    KEY_SALES_AMOUNT,
                    KEY_SALES_RECEIVED,
                    KEY_SALES_RECEIVED,
                    TABLE_SALES,
                    KEY_SALES_CUSTID,
                    custID
            );

        } else
            SALESDATA_SUM_SALES_QUERY = String.format("select sum(%s) as %s,sum(%s) as %s from %s where %s=%s and %s between '%s' and '%s'",
                    KEY_SALES_AMOUNT,
                    KEY_SALES_AMOUNT,
                    KEY_SALES_RECEIVED,
                    KEY_SALES_RECEIVED,
                    TABLE_SALES,
                    KEY_SALES_CUSTID,
                    custID,
                    KEY_SALES_DATE,
                    fromDate + " 00:00:00",
                    toDate + " 23:59:59"

            );

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SALESDATA_SUM_SALES_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    data.add(cursor.getString(cursor.getColumnIndex(KEY_SALES_AMOUNT)));
                    data.add(cursor.getString(cursor.getColumnIndex(KEY_SALES_RECEIVED)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Sales Data from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return data;
    }

    List<String> getCostDate(String fromDate, String toDate) {
        List<String> data = new ArrayList<>();

        String QUERY;
        QUERY = String.format("select DISTINCT substr(date,1,10) as date from costdata where date between '%s' and '%s' order by date(date)",
                fromDate + " 00:00:00",
                toDate + " 23:59:59"
        );
        Log.d(TAG, QUERY);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    data.add(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_DATE)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Cost date from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return data;
    }

    List<String> getItemTotalItems(int customerID) {
        List<String> data = new ArrayList<>();

        String QUERY = null;
        if (customerID > 0) {
            QUERY = String.format("select distinct productID from SALES where SALES.customerID=%s and productID != '' order by productID",
                    customerID);
        } else {
            QUERY = String.format("select distinct productID from SALES where productID != '' order by productID",
                    customerID);
        }
        Log.d(TAG, QUERY);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    data.add(cursor.getString(cursor.getColumnIndex(KEY_SALES_PRODID)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get ItemTotalItems from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return data;
    }

    List<String> getItemTotal(int customerID, int productID, String fromDate, String toDate) {
        List<String> data = new ArrayList<>();

        String QUERY = null;
        if (customerID > 0) {
            QUERY = String.format("select productName,sum(quantity) as 'quantity', sum(amount) as 'amount' from SALES left join products on SALES.productID = products.pid " +
                            "where productID = %s and SALES.customerID = %s and date between '%s' and '%s'",
                    productID,
                    customerID,
                    fromDate + " 00:00:00",
                    toDate + " 23:59:59"
            );
        } else {
            QUERY = String.format("select productName,sum(quantity) as 'quantity', sum(amount) as 'amount' from SALES left join products on SALES.productID = products.pid " +
                            "where productID = %s and date between '%s' and '%s'",
                    productID,
                    fromDate + " 00:00:00",
                    toDate + " 23:59:59"
            );
        }
        Log.d(TAG, QUERY);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    data.add(cursor.getString(cursor.getColumnIndex(KEY_PRODUCT_NAME)));
                    data.add(cursor.getString(cursor.getColumnIndex("quantity")));
                    data.add(cursor.getString(cursor.getColumnIndex("amount")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get ItemTotalItems from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return data;
    }

    List<String> getCostSumbyDate(String fromDate, String toDate) {
        List<String> data = new ArrayList<>();

        String QUERY;
        if (fromDate == null && toDate == null) {
            QUERY = String.format("select sum(%s) as %s from %s",
                    KEY_COSTDATA_AMOUNT,
                    KEY_COSTDATA_AMOUNT,
                    TABLE_COSTDATA);

        } else
            QUERY = String.format("select sum(%s) as %s from %s where %s between '%s' and '%s'",
                    KEY_COSTDATA_AMOUNT,
                    KEY_COSTDATA_AMOUNT,
                    TABLE_COSTDATA,
                    KEY_COSTDATA_DATE,
                    fromDate + " 00:00:00",
                    toDate + " 23:59:59"
            );
        Log.d(TAG, QUERY);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    data.add(cursor.getString(cursor.getColumnIndex(KEY_COSTDATA_AMOUNT)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Caost Data SUM from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return data;
    }

    List<String> getSalessumfortheDay(int custID, String date) {
        List<String> data = new ArrayList<>();
        String SALESDATA_SUM_SALES_QUERY;
        SALESDATA_SUM_SALES_QUERY = String.format("Select sum(%s) as %s, sum(%s) as %s from %s where %s between '%s and '%s and %s=%s",
                KEY_SALES_AMOUNT,
                KEY_SALES_AMOUNT,
                KEY_SALES_RECEIVED,
                KEY_SALES_RECEIVED,
                TABLE_SALES,
                KEY_SALES_DATE,
                date + " 00:00:00'",
                date + " 23:59:59'",
                KEY_SALES_CUSTID,
                custID
        );

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SALESDATA_SUM_SALES_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    data.add(cursor.getString(cursor.getColumnIndex(KEY_SALES_AMOUNT)));
                    data.add(cursor.getString(cursor.getColumnIndex(KEY_SALES_RECEIVED)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Sales Data from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return data;
    }

    int updateQtyRcvd(String[] value) {
        SQLiteDatabase db = this.getWritableDatabase();
        Integer retID = 0;

        ContentValues values = new ContentValues();
        values.put(KEY_SALES_QUANTITY, value[0]);
        values.put(KEY_SALES_AMOUNT, value[1]);
        values.put(KEY_SALES_RECEIVED, value[2]);
        values.put(KEY_SALES_COMMENTS, value[3]);

        try {
            retID = db.update(TABLE_SALES, values, KEY_SALES_SLNO + " = ?",
                    new String[]{String.valueOf(value[4])});

        } catch (Exception e) {
            e.printStackTrace();
        }


        return retID;
    }

    int updateCostQtyRcvd(String[] value) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;

        ContentValues values = new ContentValues();
        values.put(KEY_COSTDATA_QUANTITY, value[0]);
        values.put(KEY_COSTDATA_AMOUNT, value[1]);
        values.put(KEY_COSTDATA_COMMENTS, value[2]);

        try {
            retID = db.update(TABLE_COSTDATA, values, KEY_COSTDATA_SLNO + " = ?",
                    new String[]{String.valueOf(value[3])});

        } catch (Exception e) {
            e.printStackTrace();
        }


        return retID;
    }

    void salesRow(String slno) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(TABLE_SALES, KEY_SALES_SLNO + " = ?", new String[]{slno});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while deleting Sales Entry " + e);
        } finally {
            db.endTransaction();
        }
    }

    void deleteCostRow(String slno) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            db.delete(TABLE_COSTDATA, KEY_COSTDATA_SLNO + " = ?", new String[]{slno});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while deleting Cost Entry " + e);
        } finally {
            db.endTransaction();
        }
    }

    //Reset All transactions
    int resetTransactions() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        int i = 0;
        try {
            // Order of deletions is important when foreign key relationships exist.
            i = db.delete(TABLE_SALES, null, null);
            db.setTransactionSuccessful();
            System.out.println("**** Query Status = " + i);

        } catch (Exception e) {
            i = -1;
            Log.d(TAG, "Error while trying to delete all customers and products");
        } finally {
            db.endTransaction();
        }
        return i;
    }

    int resetCustomerPrice() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        int i = 0;
        try {
            // Order of deletions is important when foreign key relationships exist.
            i = db.delete(TABLE_PRICE_CUSTOMER, null, null);
            db.setTransactionSuccessful();
            System.out.println("**** Query Status = " + i);

        } catch (Exception e) {
            i = -1;
            Log.d(TAG, "Error while trying to delete all customers and products");
        } finally {
            db.endTransaction();
        }
        return i;
    }

    int resetCustomerProduct() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        int i = 0;
        try {
            // Order of deletions is important when foreign key relationships exist.
            i = db.delete(TABLE_CUSTOMER_PRODUCTS, null, null);
            db.setTransactionSuccessful();
            System.out.println("**** Query Status = " + i);

        } catch (Exception e) {
            i = -1;
            Log.d(TAG, "Error while trying to delete all customers and products");
        } finally {
            db.endTransaction();
        }
        return i;
    }

    int resetProduct() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        int i = 0;
        try {
            // Order of deletions is important when foreign key relationships exist.
            i = db.delete(TABLE_PRODUCTS, null, null);
            db.setTransactionSuccessful();
            System.out.println("**** Query Status = " + i);

        } catch (Exception e) {
            i = -1;
            Log.d(TAG, "Error while trying to delete all customers and products");
        } finally {
            db.endTransaction();
        }
        return i;
    }

    int resetCustomer() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        int i = 0;
        try {
            i = db.delete(TABLE_CUSTOMERS, null, null);
            db.setTransactionSuccessful();
            System.out.println("**** Query Status = " + i);

        } catch (Exception e) {
            i = -1;
            Log.d(TAG, "Error while trying to delete all customers and products");
        } finally {
            db.endTransaction();
        }
        return i;
    }

    String getFirstEntry() {
        String data = null;

        String SELECT_FIRST_ENTRY;
        SELECT_FIRST_ENTRY = String.format("SELECT %s FROM %s ORDER BY %s LIMIT 1",
                KEY_SALES_DATE,
                TABLE_SALES,
                KEY_SALES_DATE
        );
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_FIRST_ENTRY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    data = cursor.getString(cursor.getColumnIndex(KEY_SALES_DATE));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Sales Data from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return data;
    }


    List<Double> getSalesByYear(String fyear, String tyear, String fmonth,String tmonth, String enddate, int cusId) {
        List<Double> data = new ArrayList<>();
        String SALESDATA_SUM_SALES_YEAR = "";

        if (cusId > 0) {
            SALESDATA_SUM_SALES_YEAR = String.format("select sum(%s) as %s,sum(%s) as %s from %s where %s between '%s' and '%s' and %s = %s",
                    KEY_SALES_AMOUNT,
                    KEY_SALES_AMOUNT,
                    KEY_SALES_RECEIVED,
                    KEY_SALES_RECEIVED,
                    TABLE_SALES,
                    KEY_SALES_DATE,
                    fyear + "-" + fmonth + "-01",
                    tyear + "-" + tmonth + "-" + enddate,
                    KEY_SALES_CUSTID,
                    cusId
            );
        } else {

            SALESDATA_SUM_SALES_YEAR = String.format("select sum(%s) as %s,sum(%s) as %s from %s where %s between '%s' and '%s'",
                    KEY_SALES_AMOUNT,
                    KEY_SALES_AMOUNT,
                    KEY_SALES_RECEIVED,
                    KEY_SALES_RECEIVED,
                    TABLE_SALES,
                    KEY_SALES_DATE,
                    fyear + "-" + fmonth + "-01",
                    tyear + "-" + tmonth + "-" + enddate);
        }

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        System.out.println(SALESDATA_SUM_SALES_YEAR);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SALESDATA_SUM_SALES_YEAR, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    data.add(cursor.getDouble(cursor.getColumnIndex(KEY_SALES_AMOUNT)));
                    data.add(cursor.getDouble(cursor.getColumnIndex(KEY_SALES_RECEIVED)));

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Sales Data from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return data;
    }
}
