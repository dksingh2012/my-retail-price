const CORS={"Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"Content-Type","Access-Control-Allow-Methods":"GET,POST,OPTIONS","Content-Type":"application/json"};
const json=(x,s=200)=>new Response(JSON.stringify(x),{status:s,headers:CORS});

export default {
  async fetch(req,env){
    const u=new URL(req.url);
    if(req.method==="OPTIONS") return new Response(null,{headers:CORS});

    try{
      if(u.pathname==="/health")
        return json({ok:true,app:"MY RETAIL PRICE",market:"Gurugram",pincode:"122001"});

      if(u.pathname==="/api/sources"){
        return json({
          location:"Gurugram",
          pincode:"122001",
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

      if(u.pathname==="/api/compare"){
        const q=(u.searchParams.get("q")||"").trim();
        if(!q) return json({error:"q is required"},400);
        const r=await env.DB.prepare(
          "SELECT p.id,p.name,p.pack,p.brand,o.retailer,o.price,o.pincode,o.updated_at FROM products p JOIN offers o ON o.product_id=p.id WHERE p.name LIKE ? AND o.available=1 ORDER BY o.price ASC"
        ).bind("%"+q+"%").all();
        return json({query:q,location:"Gurugram",pincode:"122001",offers:r.results||[],data_status:"demo"});
      }

      if(u.pathname==="/api/basket" && req.method==="POST"){
        const body=await req.json();
        const queries=[...new Set((body.items||[]).map(x=>String(x.q||"").trim()).filter(Boolean))].slice(0,10);
        const data=[];

        for(const q of queries){
          const r=await env.DB.prepare(
            "SELECT p.id,p.name,p.pack,p.brand,o.retailer,o.price FROM products p JOIN offers o ON o.product_id=p.id WHERE p.name LIKE ? AND o.available=1 ORDER BY o.price ASC"
          ).bind("%"+q+"%").all();
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
          location:"Gurugram",
          pincode:"122001",
          data_status:"demo",
          items:data,
          cheapest_split_basket:{total:splitTotal,items:split},
          cheapest_single_store:single[0]||null,
          unmatched:data.filter(x=>!x.offers.length).map(x=>x.q)
        });
      }

      return json({
        app:"MY RETAIL PRICE",
        message:"API online",
        endpoints:["/health","/api/sources","/api/compare?q=milk","POST /api/basket"]
      });
    }catch(e){
      return json({error:String(e?.message||e)},500);
    }
  }
};
