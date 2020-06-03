using System;
using System.Collections.Generic;
using MySql.Data.MySqlClient;
using System.Collections.Specialized;
using System.Device.Location;
using Newtonsoft.Json;
using System.Text.RegularExpressions;
using System.Reflection;

namespace DBOperations
{
    public static class zolpoDB
    {

        const int k_KM2Meter = 1000;
        private const double k_Meter2KM = 0.001;
        private const string k_ConnetionString = "server=localhost;database=zolpo;userid=root;password=Zolpo2018;SslMode=none;charset=utf8;";
        
        private static string mySQLEscape(string i_Str)
        {
            return Regex.Replace(i_Str, @"[\x00'""\b\n\r\t\cZ\\%_]",
                delegate (Match match)
                {
                    string v = match.Value;
                    switch (v)
                    {
                        case "\x00":            // ASCII NUL (0x00) character
                            return "\\0";
                        case "\b":              // BACKSPACE character
                            return "\\b";
                        case "\n":              // NEWLINE (linefeed) character
                            return "\\n";
                        case "\r":              // CARRIAGE RETURN character
                            return "\\r";
                        case "\t":              // TAB
                            return "\\t";
                        case "\u001A":          // Ctrl-Z
                            return "\\Z";
                        case "\'":
                            return "\\'";
                        case "":
                            return "";
                        default:
                            return "\\" + v;
                    }
                });
        }

        public static void CheckForNullFields(GetStoresByChain i_Store)
        {
            if (i_Store.chain_name == null || i_Store.chain_name == "" || i_Store.chain_name == " ")
            {
                i_Store.chain_name = "unknown";
            }
            if (i_Store.store_name == null || i_Store.store_name == "" || i_Store.store_name == " ")
            {
                i_Store.store_name = "unknown";
            }
            if (i_Store.city_name == null || i_Store.city_name == "" || i_Store.city_name == " ")
            {
                i_Store.city_name = "unknown";
            }
            if (i_Store.store_address == null || i_Store.store_address == "" || i_Store.store_address == " ")
            {
                i_Store.store_address = "unknown";
            }

        }

        public static void StartDB()
        {
            MySqlConnection conn = ConnectToDB();
            APIfunctions API = new APIfunctions();
            // list of all chains
            List<Chain> Chains = API.GetChains();
            foreach (Chain chain in Chains)
            {
                //for each cahin - get list of stores
                List<GetStoresByChain> Stores = API.GetStores(chain.chain_id);
                foreach (GetStoresByChain store in Stores)
                {
                    if (!(store.store_id.Equals(null)))
                    {
                        //for each store in chain- insert data into DB
                        try
                        {
                            CheckForNullFields(store);
                            string insertQuery = string.Format(@"insert into
Store (ChainName, StoreName, StoreID, GPSLat, GPSLng, StoreCity, StoreAddress) 
VALUES('{0}','{1}','{2}','{3}','{4}','{5}','{6}');",
                            mySQLEscape(store.chain_name), mySQLEscape(store.store_name),
                            store.store_id, store.store_gps_lat,
                            store.store_gps_lng, mySQLEscape(store.city_name), mySQLEscape(store.store_address));
                            MySqlCommand insertCommand = new MySqlCommand(insertQuery, conn);

                            if (insertCommand.ExecuteNonQuery() == 1)
                            {
                               Console.WriteLine("In ZolpoDB :: StartDB. Insert succeeded");
                            }
                            else
                            {
                                Console.WriteLine("In ZolpoDB :: StartDB. Insert failed for Store ID:{0}", store.store_id);
                            }
                        }
                        catch (MySqlException ex)
                        {
                            Console.WriteLine("In ZolpoDB::StartDB. Exception: " + ex.Message);
                        }
                    }
                }
            }
            CloseConnectionToDB(conn);
        }

        public static void DeleteAllDBRecords()
        {
            string deleteQuery = "DELETE FROM store;";

            try
            {
                MySqlConnection conn = ConnectToDB();
                MySqlCommand deleteCommand = new MySqlCommand(deleteQuery, conn);

                if (deleteCommand.ExecuteNonQuery() != 0)
                {
                    Console.WriteLine("In ZolpoDB :: DeleteAllDBRecords. Delete succeeded");
                }
                else
                {
                    Console.WriteLine("In ZolpoDB :: DeleteAllDBRecords. Delete failed");
                }

                CloseConnectionToDB(conn);
            }
            catch (MySqlException ex)
            {
                Console.WriteLine(ex.Message);
            }
        }

        public static void DeleteCache()
        {
            string deleteQuery = "DELETE FROM ProductInStore;";

            try
            {
                MySqlConnection conn = ConnectToDB();
                MySqlCommand deleteCommand = new MySqlCommand(deleteQuery, conn);

                if (deleteCommand.ExecuteNonQuery() != 0)
                {
                    Console.WriteLine("In ZolpoDB :: DeleteCache. Delete succeeded");
                }
                else
                {
                    Console.WriteLine("In ZolpoDB :: DeleteCache. Delete failed");
                }

                CloseConnectionToDB(conn);
            }
            catch (MySqlException ex)
            {
                Console.WriteLine(ex.Message);
            }
        }
        public static List<ProductDetails> GetProductsInRadius(int i_RadiusInKM, string i_Barcode,
            double i_Lat, double i_Lng)
        {
            APIfunctions API = new APIfunctions();
            List<GetPriceByProductBarCode> ProductsFromAPI = new List<GetPriceByProductBarCode>();

            //list of ID's of stores that are in requested radius.
            //(from the DB)
            Dictionary<string, double> storesInRadius = GetStoresIDList(i_Lng, i_Lat, i_RadiusInKM);
            List<ProductDetails> Products = GetProductsFromDB(storesInRadius, i_Barcode);

            //if we didnt find the entire data in DB then we need to get the rest of it from the API
            if (storesInRadius.Count > 0)
            {
                try // If API is not working well - we will throw Exception 
                {
                    ProductsFromAPI = API.GetListOfProducts(storesInRadius, i_Barcode);
                    List<ProductDetails> resProduct = CreateProductDetailsFromProductsAPI(ProductsFromAPI);
                    Products.AddRange(resProduct);
                    UpdateDBWithProducts(ProductsFromAPI, i_Barcode);
                }
                catch (ArgumentException ArgEx)
                {
                    throw ArgEx; // problems with NameValueCollection
                }
                catch (Exception Ex)
                {
                    throw Ex; //API problems
                }
            }

            return Products;
        }

        public static List<ProductDetails> CreateProductDetailsFromProductsAPI(List<GetPriceByProductBarCode> i_ProductAPI)
        {
            List<ProductDetails> res = new List<ProductDetails>();

            foreach (GetPriceByProductBarCode product in i_ProductAPI)
            {
                ProductDetails temp = new ProductDetails();
                temp.distance = product.distance;
                temp.productName = product.product_name;
                temp.productPrice = double.Parse(product.store_product_price);
                temp.fullAddress = ProductDetails.GetFullAddress(product.city_name, product.store_address);
                temp.fullStoreName = ProductDetails.GetFullStoreName(product.chain_name, product.store_name);
                temp.productImageURL = ProductDetails.GetProductImageURL(product.product_image, product.product_barcode);
                temp.chainImageURL = ProductDetails.GetChainImageURL(product.sub_chain_id, product.chain_id);

                //if there are sales on product
                if (product.promo.Count > 0)
                {
                    int j = 0;
                    string promo = product.promo[j++].store_promo_description;
                    temp.promotion = promo;

                    while (j < product.promo.Count)
                    {
                        promo = Environment.NewLine + product.promo[j++].store_promo_description + Environment.NewLine;
                        temp.promotion += promo;
                    }
                }

                res.Add(temp);
            }

            return res;
        }

        public static void UpdateDBWithProducts(List<GetPriceByProductBarCode> i_Products, string i_Barcode)
        {
            MySqlConnection conn = ConnectToDB();

            foreach (GetPriceByProductBarCode Product in i_Products)
            {
                string promo;

                if (Product.promo.Count == 0)
                {
                    promo = null;
                }
                else
                {
                    promo = mySQLEscape(Product.promo[0].store_promo_description);
                }
                string insertQuery = string.Format(@"insert into 
ProductInStore (StoreID, ProductName, ProductBarcode, ProductPrice, 
ProductPromo, SubChainID, ChainID) VALUES('{0}','{1}','{2}','{3}','{4}', '{5}', '{6}');",
Product.store_id, mySQLEscape(Product.product_name), i_Barcode,
Product.store_product_price, promo, Product.sub_chain_id, Product.chain_id);

                MySqlCommand insertCommand = new MySqlCommand(insertQuery, conn);

                if (insertCommand.ExecuteNonQuery() == 1)
                {
                    Console.WriteLine("In ZolpoDB :: updateDBWithProducts. Insert to DB Succeeded");
                }
                else
                {
                    Console.WriteLine("In ZolpoDB :: updateDBWithProducts. Insert to DB failed");
                }
            }
            CloseConnectionToDB(conn);
        }

        public static List<ProductDetails> GetProductsFromDB(Dictionary<string, double> storesInRadius, string barcode)
        {
            List<ProductDetails> RES = new List<ProductDetails>();
            MySqlConnection conn = ConnectToDB();
            double distance = 0;
            string sql = @"SELECT ProductInStore.StoreID, ProductPrice, ProductPromo, ChainName, StoreName,
StoreCity, StoreAddress, ProductName, SubChainID, ChainID
FROM ProductInStore INNER JOIN Store
ON ProductInStore.StoreID = Store.StoreID
WHERE ProductInStore.ProductBarcode = " + barcode;
            MySqlCommand cmd = new MySqlCommand(sql, conn);
            MySqlDataReader rdr = cmd.ExecuteReader();

            if (rdr.HasRows)
            {
                while (rdr.Read() && (!(rdr.GetString("StoreID").Equals(string.Empty))))
                {
                    string storeID = rdr.GetString("StoreID");
                    if ( storesInRadius.TryGetValue(storeID, out distance))
                    { 
                            ProductDetails product = new ProductDetails();
                            product.distance = distance;
                            product.productPrice = rdr.GetDouble("ProductPrice");
                            product.fullStoreName = ProductDetails.GetFullStoreName(rdr.GetString("ChainName"), rdr.GetString("StoreName"));
                            product.fullAddress = ProductDetails.GetFullAddress(rdr.GetString("StoreCity"), rdr.GetString("StoreAddress"));
                            product.productName = rdr.GetString("ProductName");
                            product.productImageURL = ProductDetails.GetProductImageURL(null, barcode);
                            product.chainImageURL = ProductDetails.GetChainImageURL(rdr.GetString("SubChainID"), rdr.GetString("ChainID")); 

                            //if there are sales on product
                            string promo = rdr.GetString("ProductPromo");
                            if (!(promo.Equals(string.Empty)))
                            {
                                product.promotion = promo;
                            }

                            RES.Add(product);
                            storesInRadius.Remove(storeID); // the StoreID was in the list so we need to delete it
                        }
                    else
                    {
                       Console.WriteLine("In ZolpoDB :: GetProductsFromDB. " + storeID + "was not in list");
                    }
                }
                
            }
            rdr.Close();
            CloseConnectionToDB(conn);

            return RES;
        }

        public static void CloseConnectionToDB(MySqlConnection i_Conn)
        {
            if (i_Conn != null)
            {
                i_Conn.Close();
                Console.WriteLine("In ZolpoDB :: closeConnectionToDB. closing connection to MySQL DB");
            }
        }

        public static MySqlConnection ConnectToDB()
        {
            MySqlConnection conn = new MySqlConnection(k_ConnetionString);
            conn.Open();
            return conn;
        }

        public static Dictionary<string, double> GetStoresIDList
            (double sLongitude, double sLatitude, int radiusInKM)
        {
            MySqlConnection conn = ConnectToDB();
            string sql = "SELECT GPSLat, GPSLng, StoreID FROM Store";
            MySqlCommand cmd = new MySqlCommand(sql, conn);
            MySqlDataReader rdr = cmd.ExecuteReader();
            Dictionary<string, double> res = new Dictionary<string, double>();

            GeoCoordinate sCoord = new GeoCoordinate(sLatitude, sLongitude);

            if (rdr.HasRows)
            {
                while (rdr.Read() && (!(rdr.GetString("GPSLat").Equals(string.Empty))))
                {
                    double eLatitude = rdr.GetDouble("GPSLat");
                    double eLongitude = rdr.GetDouble("GPSLng");
                    GeoCoordinate eCoord = new GeoCoordinate(eLatitude, eLongitude);

                    // distance is in meters
                    double distance = sCoord.GetDistanceTo(eCoord);

                    // convert distance to kilometers
                    distance *= k_Meter2KM;
                    if (distance <= radiusInKM)
                    {
                        res.Add(rdr.GetString("StoreID"), distance);
                    }
                }
            }
            rdr.Close();
            CloseConnectionToDB(conn);

            return res;
        }
    }
}