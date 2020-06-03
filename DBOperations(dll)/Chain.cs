using System.Collections.Generic;

namespace DBOperations
{
    public class Chain
    {
        public string chain_id { get; set; }
        public string chain_name { get; set; }
        public string chain_code { get; set; }
        public string chain_image { get; set; }
        public List<SubChain> sub_chain { get; set; }
    }
}
