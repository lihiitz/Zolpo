using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Collections.Specialized;
using Newtonsoft.Json;

namespace DBOperations
{
    public class APIfunctions
    {
        private const string k_Url = "https://api.superget.co.il/";
        private NameValueCollection data;
        private int retryCount = 2;

        private void setAPIData()
        {
            data.Add("api_key", "a5f4187894d0071aee6a523acba225df621ed9f7");
        }

        public string Url
        {
            get
            {
                return k_Url;
            }
        }

        public APIfunctions()
        {
            data = new NameValueCollection();
            setAPIData();
        }

        public List<GetPriceByProductBarCode> GetListOfProducts(Dictionary<string, double> i_StoreId, string i_Barcode)
        {
            List<GetPriceByProductBarCode> Chains = new List<GetPriceByProductBarCode>();

            data.Set("action", "GetPriceByProductBarCode");
            data.Set("product_barcode", i_Barcode);

            foreach (KeyValuePair<string, double> kvp in i_StoreId)
            {
                Console.WriteLine("In APIfunctions :: GetListOfProducts. requesting data for storeID: " + kvp.Key);
                data.Set("store_id", kvp.Key);
                GetJsonResponse(Chains, kvp);
            }

            return Chains;
        }

        public string SendPostToUrl()
        {
            string responseString = null;
            var client = new WebClient();
            var response = client.UploadValues(Url, data);
            responseString = Encoding.Default.GetString(response);
            return responseString;
        }

        public void GetJsonResponse(List<GetPriceByProductBarCode> list, KeyValuePair<string, double> i_Kvp)
        {
            string json = null;
            int currentRetry = 0;

            do
            {
                try
                {
                    json = SendPostToUrl();

                    if (!(json.Contains("error")))
                    {
                        AddProductToList(list, json, i_Kvp.Value);
                    }
                    return;
                }
                catch (WebException webEx)
                {
                    currentRetry++;
                    {
                        Console.WriteLine("In APIfunctions :: GetJsonResponse. " + webEx.ToString());
                        if (currentRetry < retryCount)
                        {
                            Console.WriteLine("Retry for StoreID {0}", i_Kvp.Key);
                        }
                    }
                }
            }
            while (currentRetry < this.retryCount);
        }

        public void AddProductToList (List<GetPriceByProductBarCode> i_List, string i_Json, double i_Value)
        {
            List<GetPriceByProductBarCode> temp =
            JsonConvert.DeserializeObject<List<GetPriceByProductBarCode>>(i_Json);
            temp[0].distance = i_Value;
            i_List.Add(temp[0]);
            temp.Clear();
        }

        public List<Chain> GetChains()
        {
            data.Set("action", "GetChains");
            var responseJson = SendPostToUrl();
            List<Chain> Chains = JsonConvert.DeserializeObject<List<Chain>>(responseJson);
            return Chains;
        }

        public List<GetStoresByChain> GetStores(string i_ChainID)
        {
            List<GetStoresByChain> res;

            data.Set("action", "GetStoresByChain");
            data.Set("chain_id", i_ChainID);
            var responseJson = SendPostToUrl();
            if (!(responseJson.StartsWith("{\"error_text\"")))
            {
                res = JsonConvert.DeserializeObject<List<GetStoresByChain>>(responseJson);
            }
            else
            {
                res = new List<GetStoresByChain>();
            }

            return res;
        }

        public List<GetPrice> GetProducts(string i_StoreID)
        {
            data.Set("action", "GetPrice");
            data.Set("store_id", i_StoreID);
            var responseJson = SendPostToUrl();
            List<GetPrice> ProductData = JsonConvert.DeserializeObject<List<GetPrice>>(responseJson);

            return ProductData;
        }
    }
}