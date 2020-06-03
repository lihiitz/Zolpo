using DBOperations;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace ZolpoServer.Controllers
{
    public class ProductDetailsController : ApiController
    {
        private const string k_ProductDoesNotExistMsg = "מצטערים, המוצר לא קיים במערכת";
        private const string k_CommunicationExceptionMsg = "בעיית תקשורת. אנא נסו במועד מאוחר יותר";
        private const string k_GeneralErrorMsg = "שגיאה";
        private const string k_Error = "{ \"Message\": \"עדכון התמונה נכשל\" }";
        private const string k_Success = "{ \"Message\": \"התמונה הועלתה בהצלחה\" }";
        private const string k_Path = @"C:\inetpub\wwwroot\ZolpoServer\Images";

        public HttpResponseMessage Get([FromUri]string Barcode, [FromUri]string Latitude, [FromUri]string Longitude, [FromUri]string Distance)
        {
            int radiusInKm;
            double lat, lng;
            HttpError httpError;
            HttpResponseMessage res;

            try
            {
                int.TryParse(Distance, out radiusInKm);
                double.TryParse(Latitude, out lat);
                double.TryParse(Longitude, out lng);

                List<ProductDetails> Products = zolpoDB.GetProductsInRadius(radiusInKm, Barcode, lat, lng);
                if (Products.Count == 0)
                {
                    httpError = new HttpError(k_ProductDoesNotExistMsg);
                    res = Request.CreateErrorResponse(HttpStatusCode.NotFound, httpError);
                }
                else
                {
                    res = Request.CreateResponse(HttpStatusCode.OK, Products);
                }
            }
            catch (ArgumentException ArgEx)
            {
                httpError = new HttpError(k_GeneralErrorMsg);
                res = Request.CreateErrorResponse(HttpStatusCode.NotFound, httpError);
            }
            catch (Exception ex)
            {
                httpError = new HttpError(k_CommunicationExceptionMsg);
                res = Request.CreateErrorResponse(HttpStatusCode.NotFound, httpError);
            }
            return res;
        }

        public JObject Post([FromBody]Dictionary<string, string> imageData)
        {
            string res;
            //Check if directory exist
            try
            {
                if (!System.IO.Directory.Exists(k_Path))
                {
                    //Create directory if it doesn't exist. 
                    //Should not happen.
                    System.IO.Directory.CreateDirectory(k_Path);
                }
                string imageName = imageData.Values.ElementAt(1) + ".jpg";
                string imgPath = System.IO.Path.Combine(k_Path, imageName);
                string image = imageData.Values.ElementAt(0);
                byte[] imageBytes = Convert.FromBase64String(image);
                File.WriteAllBytes(imgPath, imageBytes);
                res = k_Success;
            }
            catch (Exception ex)
            {
                res = k_Error;
            }
            JObject json = JObject.Parse(res);
            return json;
        }

    }
}
