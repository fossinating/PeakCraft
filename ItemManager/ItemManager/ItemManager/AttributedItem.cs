﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ItemManager
{
    class AttributedItem : Item
    {
        Dictionary<String, String> attributes;

        public AttributedItem(String id, String oreDict, String displayName, int rarity, String description, Material material, String type, String ability) 
            : base(id, oreDict, displayName, rarity, description, material, type, ability)
        {
            attributes = new Dictionary<string, string>();
        }

        public AttributedItem(Item item) : base(item)
        {
            attributes = new Dictionary<string, string>();
        }

        public static new Item fromDictionary(Dictionary<string, string> data)
        {
            AttributedItem item =  new AttributedItem(
                            data["id"],
                            data["oreDict"],
                            data["displayName"],
                            Int32.Parse(data["rarity"]),
                            data["description"],
                            new Material(data["materialID"]),
                            data["type"],
                            data.ContainsKey("ability") ? data["ability"] : ""
                            );


            foreach (string key in new string[]{ "id", "oreDict", "displayName", "rarity", "description", "materialID", "type", "ability" })
            {
                data.Remove(key);
            }

            foreach (string key in data.Keys)
            {
                item.attributes.Add(key, data[key]);
            }

            return item;
        }

        public new Dictionary<string, string> toDictionary()
        {
            Dictionary<string, string> data = base.toDictionary();

            foreach (string key in attributes.Keys)
            {
                data.Add(key, attributes[key].ToString());
            }

            return data;
        }

        public void setAttribute(string name, string value)
        {
            attributes[name] = value;
        }

        public string getAttributeOrDefault(string name, string defaultValue)
        {
            return attributes.ContainsKey(name) ? attributes[name] : defaultValue;
        }
    }
}
