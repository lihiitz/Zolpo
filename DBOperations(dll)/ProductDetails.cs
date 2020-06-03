using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DBOperations
{
    public class ProductDetails
    {
        private const string k_ImagesUrlPrefix = "http://192.116.98.71:8082/Images/";
        private const string k_ImagesUrlSuffix = ".jpg";
        public double productPrice { get; set; }
        public string productName { get; set; }
        public string productImageURL { get; set; }
        public double distance { get; set; }
        public string fullAddress { get; set; }
        public string fullStoreName { get; set; }
        public string chainImageURL { get; set; }
        public string promotion { get; set; }

        public static string GetFullStoreName(string i_Chain, string i_Store)
        {
            if (i_Chain == "unknown" && i_Store == "unknown")
            {
                return ("שם חנות לא ידוע");
            }
            else if (i_Chain == "unknown")
            {
                return (i_Store);
            }
            else if (i_Store == "unknown")
            {
                return (i_Chain);
            }
            else
            {
                return (i_Chain + ", " + i_Store);
            }
        }

        public static string GetFullAddress(string i_City, string i_Addresss)
        {
            if (i_City == "unknown" && i_Addresss == "unknown")
            {
                return ("כתובת לא ידועה");
            }
            else if (i_Addresss == "unknown")
            {
                return (i_City);
            }
            else if (i_City == "unknown")
            {
                return (i_Addresss);
            }
            else
            {
                return (i_Addresss + ", " + i_City);
            }
        }

        public static string GetProductImageURL(string i_ProductImage, string i_ProductBarcode = null)
        {
            string productImageUrl = string.Empty;

            if (i_ProductImage != null && i_ProductImage != string.Empty)
            {
                productImageUrl = i_ProductImage;
            }
            else
            {
                if (i_ProductBarcode != null)
                {
                    productImageUrl = k_ImagesUrlPrefix + i_ProductBarcode + k_ImagesUrlSuffix;
                }
            }

            return productImageUrl;
        }

        public static string GetChainImageURL(string i_SubChainId, string i_ChainId)
        {
            string chainImageUrl = string.Empty;

            if (i_SubChainId != null && i_SubChainId != string.Empty)
            {
                chainImageUrl = k_ImagesUrlPrefix + i_SubChainId + k_ImagesUrlSuffix;
            }
            else if (i_ChainId != null && i_ChainId != string.Empty)
            {
                chainImageUrl = k_ImagesUrlPrefix + i_ChainId + k_ImagesUrlSuffix;
            }

            return chainImageUrl;
        }
    }
}
