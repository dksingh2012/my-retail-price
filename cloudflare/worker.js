const CORS={"Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"Content-Type","Access-Control-Allow-Methods":"GET,POST,OPTIONS","Content-Type":"application/json"};
const json=(x,s=200)=>new Response(JSON.stringify(x),{status:s,headers:CORS});
const MARKET="Gurugram";
const PIN="122001";
// Gurugram city-center coordinates; actual serviceability is checked by the provider.
const LAT="28.4595";
const LON="77.0266";
const LIVE_PLATFORMS=["BlinkIt","Zepto","BigBasket","JioMart","Amazon","Flipkart"];

// Keep provider search broad enough to find products, then remove clearly
// irrelevant matches locally. All query terms must match the product text.
// A numeric size token such as "200ml" is treated as a preference rather than
// a hard requirement so products with equivalent pack formatting are retained.
// Normalize electronics identifiers from retailer titles for exact-match scoring.
function normalizeProductIdentity(o){
  const raw=[o.brand,o.name,o.pack].filter(Boolean).join(" ");
  const text=raw.toLowerCase().replace(/[^a-z0-9.\-+/ ]+/g," ");
  const modelTokens=(text.match(/\b[a-z]{1,6}[-/]?\d{2,}[a-z0-9/-]*\b/gi)||[]).map(x=>x.toLowerCase());
  const capacity=(text.match(/\b\d+(?:\.\d+)?\s*(?:l|litre|liter|kg|g|ml|inch|in|ton|tb|gb|mb)\b/gi)||[]).map(x=>x.replace(/\s+/g,""));
  return {...o,identity:{brand:String(o.brand||"").toLowerCase().trim(),models:[...new Set(modelTokens)],capacity:[...new Set(capacity)]}};
}

function classifyMatch(o,q){
  const text=[o.name,o.brand,o.pack].filter(Boolean).join(" ").toLowerCase();
  const query=q.toLowerCase();
  const queryModels=(query.match(/\b[a-z]{1,6}[-/]?\d{2,}[a-z0-9/-]*\b/gi)||[]).map(x=>x.toLowerCase());
  const offerModels=o.identity?.models||[];
  const modelExact=queryModels.length>0 && queryModels.every(m=>offerModels.includes(m));
  const brandQuery=(q.match(/^[a-z]+/i)?.[0]||"").toLowerCase();
  const brandExact=!!o.identity?.brand && (query.includes(o.identity.brand) || !brandQuery || o.identity.brand===brandQuery);
  const queryCaps=(query.match(/\b\d+(?:\.\d+)?\s*(?:l|litre|liter|kg|g|ml|inch|in|ton|tb|gb)\b/gi)||[]).map(x=>x.replace(/\s+/g,"").toLowerCase());
  const offerCaps=o.identity?.capacity||[];
  const capacityExact=queryCaps.length===0 || queryCaps.every(c=>offerCaps.includes(c));
  if(modelExact && brandExact && capacityExact) return "exact_match";
  if(modelExact || (brandExact && capacityExact)) return "close_match";
  return "alternative";
}

function filterRelevantOffers(offers,q){
  const tokens=q.toLowerCase().replace(/[^a-z0-9.]+/g," ").trim().split(/\s+/).filter(Boolean);
  if(!tokens.length) return offers;
  const scored=offers.map(o=>{
    const text=[o.name,o.brand,o.pack].filter(Boolean).join(" ").toLowerCase();
    let score=0, matched=0;
    for(const token of tokens){
      const numeric=/^\d+(?:\.\d+)?(?:ml|l|g|kg|mg|pcs?|pack|w|kw|inch|in|ton|tb|gb)?$/i.test(token);
      if(text.includes(token)){ matched++; score += numeric ? 2 : 5; }
      else if(numeric){
        const n=token.match(/^(\d+(?:\.\d+)?)(ml|l|g|kg|mg|pcs?|pack|w|kw|inch|in|ton|tb|gb)?$/i);
        if(n && text.includes(n[1])){ matched++; score += 1; }
      }
    }
    const primary=tokens.filter(t=>!/^\d/.test(t));
    const primaryMatched=primary.filter(t=>text.includes(t)).length;
    return {...o,_score:score,_matched:matched,_primaryMatched:primaryMatched,_tokenCount:tokens.length};
  });
  const primaryCount=tokens.filter(t=>!/^\d/.test(t)).length;
  const relevant=scored.filter(x=>x._primaryMatched===primaryCount && x._matched>=Math.min(tokens.length,primaryCount));
  relevant.sort((a,b)=>b._score-a._score || a.price-b.price);
  return relevant.map(({_score,_matched,_primaryMatched,_tokenCount,...o})=>o);
}



async function providerSearch(q,pincode,env,platform){
  const url=new URL("https://api.quickcommerceapi.com/v1/search");
  url.searchParams.set("q",q);
  url.searchParams.set("lat",LAT);
  url.searchParams.set("lon",LON);
  url.searchParams.set("platform",platform);
  if(pincode) url.searchParams.set("pincode",pincode);
  const headers={"X-API-Key":env.QUICKCOMMERCE_API_KEY};
  if(["JioMart","Minutes","DMart"].includes(platform)) headers["x-geolocation-pincode"]=pincode;

  let res;
  try{
    res=await fetch(url.toString(),{headers});
  }catch(e){
    return {ok:false,status:0,error:"Provider network error: "+String(e?.message||e),offers:[]};
  }

  const raw=await res.text();
  let body={};
  try{ body=raw?JSON.parse(raw):{}; }
  catch(e){
    return {ok:false,status:res.status,error:"Provider returned non-JSON response ("+res.status+")",offers:[]};
  }

  if(!res.ok || body.status!=="success"){
    return {ok:false,status:res.status,error:body?.message||body?.error||("QuickCommerce API error "+res.status),offers:[],credits_remaining:body?.credits_remaining};
  }

  const products=Array.isArray(body?.data?.products) ? body.data.products :
                 Array.isArray(body?.data?.results) ? body.data.results :
                 Array.isArray(body?.results) ? body.results : [];
  const offers=[];
  for(const item of products){
    const price=Number(item.offer_price ?? item.price);
    if(!Number.isFinite(price)) continue;
    const p=item.platform;
    offers.push({
      id:String(item.id||item.item_id||""),
      name:String(item.name||q),
      brand:String(item.brand||""),
      pack:String(item.quantity||item.weight||""),
      retailer:String((p&&typeof p==="object"?p.name:p)||platform),
      price,
      mrp:Number.isFinite(Number(item.mrp))?Number(item.mrp):null,
      pincode,
      available:item.available!==false && item.in_stock!==false,
      source:"QuickCommerce API",
      data_status:"live_authorized",
      product_url:String(item.deeplink||item.product_url||""),
      updated_at:new Date().toISOString(),
      sla:String((p&&typeof p==="object"?p.sla:"")||"")
    });
  }
  return {ok:true,offers,credits_remaining:body.credits_remaining};
}



function fallbackQueries(q){
  const cleaned=q.replace(/\s+/g," ").trim();
  const tokens=cleaned.split(" ").filter(Boolean);
  const numeric=tokens.filter(t=>/^\d+(?:\.\d+)?(?:ml|l|litre|liter|g|kg|mg|pcs?|pack|w|kw|inch|in|ton|tb|gb)?$/i.test(t));
  const words=tokens.filter(t=>!/^\d/.test(t));
  const brand=words.find(t=>!["geyser","water","heater","storage","instant","tv","mobile","phone","ac","air","conditioner","refrigerator","fridge"].includes(t.toLowerCase()));
  const capacity=numeric.find(t=>/^(?:\d+(?:\.\d+)?)(?:l|litre|liter|kg|g|ml|inch|in|ton)$/i.test(t));
  const candidates=[cleaned];
  if(brand && capacity) candidates.push(brand+" "+capacity);
  if(brand) candidates.push(brand);
  if(capacity && /geyser|water|heater/i.test(cleaned)) candidates.push(capacity+" geyser");
  // Last-resort category search: if the exact brand/model is not indexed by
  // the provider, return relevant category alternatives instead of a blank
  // comparison screen.
  const category =
    /geyser|water\s*heater|water\s*heating/i.test(cleaned) ? "geyser" :
    /refrigerator|fridge/i.test(cleaned) ? "refrigerator" :
    /air\s*conditioner|\bac\b/i.test(cleaned) ? "air conditioner" :
    /television|\btv\b/i.test(cleaned) ? "tv" :
    /mobile|phone|smartphone/i.test(cleaned) ? "mobile phone" : "";
  if(category) candidates.push(category);
  return [...new Set(candidates)];
}

async function liveSearch(q,pincode,env){
  if(!env.QUICKCOMMERCE_API_KEY) return {ok:false,error:"QUICKCOMMERCE_API_KEY is not configured"};

  // Use the individual search endpoint so one retailer's upstream failure
  // cannot break the entire comparison. Four platforms keep the free trial
  // usable while still giving a meaningful comparison.
  const platforms=["Amazon","Flipkart","BlinkIt","Zepto"];
  const all=[];
  const errors=[];
  let credits_remaining=null;

  for(const platform of platforms){
    const r=await providerSearch(q,pincode,env,platform);
    if(r.credits_remaining!=null) credits_remaining=r.credits_remaining;
    if(!r.ok){ errors.push(platform+": "+r.error); continue; }

    const normalized=r.offers.map(normalizeProductIdentity);
    const relevant=filterRelevantOffers(normalized,q);
    all.push(...(relevant.length ? relevant : normalized.slice(0,20)));
  }

  const seen=new Set();
  const unique=all.filter(o=>{
    const key=[o.retailer,o.id,o.name,o.price].join("|").toLowerCase();
    if(seen.has(key)) return false;
    seen.add(key); return true;
  });

  const relevantOffers=unique.map(o=>({...o,match_type:classifyMatch(o,q)}));
  relevantOffers.sort((a,b)=>{
    const rank=x=>x==="exact_match"?0:x==="close_match"?1:2;
    return rank(a.match_type)-rank(b.match_type) || a.price-b.price;
  });

  if(!relevantOffers.length && errors.length===platforms.length){
    return {ok:false,error:"All live retailer searches failed: "+errors.join(" | "),credits_remaining};
  }

  return {ok:true,offers:relevantOffers,credits_remaining,retailer_errors:errors};
}




const offerSelect=`
SELECT p.id,p.name,p.pack,p.brand,p.unit_value,p.unit,
       o.retailer,o.price,o.mrp,o.pincode,o.available,o.source,
       o.data_status,o.product_url,o.updated_at
FROM products p
JOIN offers o ON o.product_id=p.id
`;

export default {
  async fetch(req,env){
    const u=new URL(req.url);
    if(req.method==="OPTIONS") return new Response(null,{headers:CORS});

    try{
      if(u.pathname==="/health")
        return json({ok:true,app:"MY RETAIL PRICE",market:MARKET,pincode:PIN,data_status:env.QUICKCOMMERCE_API_KEY?"live_authorized":"demo"});

      if(u.pathname==="/api/import" && req.method==="POST"){
        const token=req.headers.get("X-Import-Token")||"";
        if(!env.IMPORT_TOKEN || token!==env.IMPORT_TOKEN) return json({error:"Unauthorized"},401);
        const body=await req.json();
        const retailer=String(body.retailer||"").trim();
        const offers=Array.isArray(body.offers)?body.offers:[];
        const allowed=["blinkit","zepto","bigbasket","jiomart","instamart"];
        if(!allowed.includes(retailer.toLowerCase())) return json({error:"Unsupported retailer"},400);
        if(!offers.length || offers.length>500) return json({error:"offers must contain 1-500 records"},400);

        const now=new Date().toISOString();
        const results=[];
        for(const x of offers){
          const name=String(x.name||"").trim();
          const brand=String(x.brand||"").trim();
          const pack=String(x.pack||"").trim();
          const price=Number(x.price);
          const pincode=String(x.pincode||PIN).trim();
          if(!name || !Number.isFinite(price) || price<0 || !pincode) continue;

          const existing=await env.DB.prepare(
            "SELECT id FROM products WHERE name=? AND IFNULL(brand,'')=IFNULL(?, '') AND IFNULL(pack,'')=IFNULL(?, '') LIMIT 1"
          ).bind(name,brand,pack).first();

          let productId=existing?.id;
          if(!productId){
            const ins=await env.DB.prepare(
              "INSERT INTO products(name,pack,brand,unit_value,unit) VALUES(?,?,?,?,?)"
            ).bind(name,pack,brand,Number.isFinite(Number(x.unit_value))?Number(x.unit_value):null,String(x.unit||"")).run();
            productId=ins.meta.last_row_id;
          }

          await env.DB.prepare(
            "INSERT INTO offers(product_id,retailer,price,mrp,pincode,available,source,data_status,product_url,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?)"
          ).bind(productId,retailer,price,Number.isFinite(Number(x.mrp))?Number(x.mrp):null,pincode,x.available===false?0:1,String(x.source||retailer),"live_authorized",String(x.product_url||""),now).run();

          results.push({product_id:productId,name,brand,pack,price,pincode});
        }

        return json({ok:true,retailer,imported:results.length,updated_at:now,data_status:"live_authorized"});
      }

      if(u.pathname==="/api/sources"){
        return json({
          location:MARKET,
          pincode:PIN,
          sources:[
            {name:"Swiggy Instamart",key:"instamart",status:"official_integration_available_pending_approval"},
            {name:"Blinkit",key:"blinkit",status:"partner_or_licensed_feed_required"},
            {name:"Zepto",key:"zepto",status:"partner_or_licensed_feed_required"},
            {name:"BigBasket",key:"bigbasket",status:"partner_or_licensed_feed_required"},
            {name:"JioMart",key:"jiomart",status:"partner_or_licensed_feed_required"}
          ],
          demo_source:{status:"active_for_mvp_testing"}
        });
      }

      if(u.pathname==="/api/products"){
        const q=(u.searchParams.get("q")||"").trim();
        if(!q) return json({error:"q is required"},400);
        const r=await env.DB.prepare(
          "SELECT id,name,pack,brand,unit_value,unit FROM products WHERE name LIKE ? OR brand LIKE ? ORDER BY name LIMIT 20"
        ).bind("%"+q+"%","%"+q+"%").all();
        return json({query:q,location:MARKET,pincode:PIN,products:r.results||[],data_status:"demo"});
      }

      if(u.pathname==="/api/compare"){
        const q=(u.searchParams.get("q")||"").trim();
        const pincode=(u.searchParams.get("pincode")||PIN).trim();
        if(!q) return json({error:"q is required"},400);
        if(env.QUICKCOMMERCE_API_KEY){
          const live=await liveSearch(q,pincode,env);
          if(live.ok && live.offers?.length){
            return json({query:q,location:MARKET,pincode,offers:live.offers,cheapest:live.offers[0]||null,data_status:"live_authorized",credits_remaining:live.credits_remaining,retailer_errors:live.retailer_errors||[]});
          }
          // Never leave the app on a blank/502 comparison screen. If the live
          // provider is temporarily unavailable, use our locally stored
          // catalogue as a clearly-labelled reference fallback.
          // Use progressively broader local catalogue queries. This makes the
          // fallback useful for real searches such as "Orient 15L geyser 2000W 5★":
          // the full sentence may not exist verbatim in the catalogue, while
          // "Orient 15L" and "geyser" do.
          const candidates=fallbackQueries(q);
          const fallbackMap=new Map();
          for(const candidate of candidates){
            const like="%"+candidate+"%";
            const fallback=await env.DB.prepare(
              offerSelect+" WHERE (p.name LIKE ? OR p.brand LIKE ? OR p.pack LIKE ?) AND o.pincode=? AND o.available=1 ORDER BY o.price ASC LIMIT 30"
            ).bind(like,like,like,pincode).all();
            for(const row of (fallback.results||[])){
              const key=[row.product_id||row.id,row.retailer,row.price,row.product_url||""].join("|");
              if(!fallbackMap.has(key)) fallbackMap.set(key,{...row,match_type:"reference"});
            }
            if(fallbackMap.size>=12) break;
          }
          const fallbackOffers=[...fallbackMap.values()].sort((a,b)=>Number(a.price||0)-Number(b.price||0));
          return json({
            query:q,location:MARKET,pincode,
            offers:fallbackOffers,
            cheapest:fallbackOffers[0]||null,
            data_status:fallbackOffers.length?"reference_fallback":"no_results",
            live_error:live.error||null,
            retailer_errors:live.retailer_errors||[],
            fallback_queries:candidates
          });
        }
        const r=await env.DB.prepare(
          offerSelect+" WHERE (p.name LIKE ? OR p.brand LIKE ?) AND o.pincode=? AND o.available=1 ORDER BY o.price ASC"
        ).bind("%"+q+"%","%"+q+"%",pincode).all();
        return json({
          query:q,location:MARKET,pincode,
          offers:r.results||[],
          cheapest:r.results?.[0]||null,
          data_status:"demo"
        });
      }

      if(u.pathname==="/api/basket" && req.method==="POST"){
        const body=await req.json();
        const pincode=String(body.pincode||PIN).trim();
        const queries=[...new Set((body.items||[]).map(x=>String(x.q||"").trim()).filter(Boolean))].slice(0,10);
        if(env.QUICKCOMMERCE_API_KEY){
          const data=[];
          for(const q of queries){
            const live=await liveSearch(q,pincode,env);
            if(!live.ok) return json({error:live.error,data_status:"live_error"},502);
            data.push({q,offers:live.offers});
          }
          const split=data.filter(x=>x.offers.length).map(x=>({q:x.q,offer:x.offers[0]}));
          const splitTotal=split.reduce((sum,x)=>sum+(Number(x.offer.price)||0),0);
          const retailers=[...new Set(data.flatMap(x=>x.offers.map(o=>o.retailer).filter(Boolean)))];
          const single=[];
          for(const retailer of retailers){
            let total=0,complete=true;
            for(const item of data){
              const offer=item.offers.find(x=>x.retailer===retailer);
              if(!offer){complete=false;break;}
              total+=Number(offer.price)||0;
            }
            if(complete) single.push({retailer,total});
          }
          single.sort((a,b)=>a.total-b.total);
          return json({
            location:MARKET,pincode,data_status:"live_authorized",items:data,
            cheapest_split_basket:{total:splitTotal,items:split},
            cheapest_single_store:single[0]||null,
            unmatched:data.filter(x=>!x.offers.length).map(x=>x.q)
          });
        }
        const data=[];
        for(const q of queries){
          const r=await env.DB.prepare(
            offerSelect+" WHERE (p.name LIKE ? OR p.brand LIKE ?) AND o.pincode=? AND o.available=1 ORDER BY o.price ASC"
          ).bind("%"+q+"%","%"+q+"%",pincode).all();
          data.push({q,offers:r.results||[]});
        }
        const split=data.filter(x=>x.offers.length).map(x=>({q:x.q,offer:x.offers[0]}));
        const splitTotal=split.reduce((sum,x)=>sum+(Number(x.offer.price)||0),0);
        const retailers=[...new Set(split.map(x=>x.offer.retailer).filter(Boolean))];
        const single=[];
        for(const retailer of retailers){
          let total=0,complete=true;
          for(const item of data){
            const offer=item.offers.find(x=>x.retailer===retailer);
            if(!offer){complete=false;break;}
            total+=Number(offer.price)||0;
          }
          if(complete) single.push({retailer,total});
        }
        single.sort((a,b)=>a.total-b.total);
        return json({
          location:MARKET,pincode,data_status:"demo",items:data,
          cheapest_split_basket:{total:splitTotal,items:split},
          cheapest_single_store:single[0]||null,
          unmatched:data.filter(x=>!x.offers.length).map(x=>x.q)
        });
      }

      return json({
        app:"MY RETAIL PRICE",
        message:"API online",
        endpoints:["/health","/api/products?q=milk","/api/sources","/api/compare?q=milk","POST /api/basket"]
      });
    }catch(e){
      return json({error:String(e?.message||e)},500);
    }
  }
};