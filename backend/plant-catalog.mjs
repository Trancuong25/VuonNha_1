// Curated references; unknown measurements remain blank instead of guessed.
export const plants = [
 {id:'zz',name:'Kim tiền',scientificName:'Zamioculcas zamiifolia',aliases:['cay kim tien','zz plant','zamioculcas'],
  matureHeight:'Khoảng 40–80 cm; thay đổi theo điều kiện trồng',humidity:'Chưa có khoảng % riêng được xác minh cho trồng tại nhà',soil:'Để giá thể khô giữa các lần tưới; tránh úng',light:'Ánh sáng tán xạ',
  source:'https://ask.ifas.ufl.edu/publication/EP252',tasks:[{name:'Kiểm tra đất',everyDays:7}]},
 {id:'orchid',name:'Lan hồ điệp',scientificName:'Phalaenopsis spp.',aliases:['lan','ho diep','phong lan','phalaenopsis'],
  matureHeight:'Tùy loài/giống và chiều dài cành hoa',humidity:'50–80% độ ẩm không khí; cần thông thoáng',soil:'Giá thể thoáng; kiểm tra trước khi tưới, tránh đọng nước',light:'Ánh sáng gián tiếp',
  source:'https://ucanr.edu/sites/default/files/2018-02/278234.pdf',tasks:[{name:'Kiểm tra giá thể',everyDays:3}]},
 {id:'monstera',name:'Trầu bà lá xẻ',scientificName:'Monstera deliciosa',aliases:['monstera','trau ba','trau ba la xe'],
  matureHeight:'Thay đổi theo không gian và giá đỡ; chưa có khoảng riêng được xác minh',humidity:'Ưu tiên độ ẩm không khí trên 50%',soil:'Kiểm tra đất trước khi tưới; thoát nước tốt',light:'Ánh sáng sáng, gián tiếp',
  source:'https://extension.psu.edu/monstera-as-a-houseplant',tasks:[{name:'Kiểm tra đất',everyDays:4}]}
];
const normalize = value => value.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/đ/g,'d').trim();
export function lookupPlants(query) {
 const q=normalize(query).replace(/^cay\s+/, '');
 if(q.length<2||q.length>120) return [];
 return plants.filter(p=>[p.name,p.scientificName,...p.aliases].some(s=> {
   const value=normalize(s); return value.startsWith(q)||value.includes(' '+q);
 })).slice(0,6);
}
