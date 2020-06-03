using System.Collections.Generic;

namespace DBOperations
{
    public class GetPriceByProductBarCode
    {
        public string store_id { get; set; }
        public string store_product_barcode { get; set; }
        public string store_product_price { get; set; }
        public string product_name { get; set; }
        public string product_barcode { get; set; }
        public string product_image { get; set; }
        public string product_is_real_barcode { get; set; }
        public string store_name { get; set; }
        public string store_address { get; set; }
        public string chain_name { get; set; }
        public string chain_image { get; set; }
        public string sub_chain_image { get; set; }
        public string chain_id { get; set; }
        public string sub_chain_id { get; set; }
        public string city_name { get; set; }
        public List<Promo> promo { get; set; }
        public string full_store_name { get; set; }
        public string promo_description { get; set; }
        public double distance { get; set; }
    }
}
