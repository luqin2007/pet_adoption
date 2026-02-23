<script setup>
import AppFooter from './components/AppFooter.vue'
import AppHeader from './components/AppHeader.vue'
import AdoptionSection from './components/AdoptionSection.vue'
import ActivitySection from './components/ActivitySection.vue'
import FeaturedArticlesSection from './components/FeaturedArticlesSection.vue'
import HeroSection from './components/HeroSection.vue'
import StatsOverview from './components/StatsOverview.vue'

const navItems = [
  { id: 'adoption', label: '领养推荐' },
  { id: 'news', label: '近期活动' },
  { id: 'articles', label: '精选文章' },
]

const stats = [
  { label: '累计救助', value: 1268, suffix: '只' },
  { label: '成功领养', value: 873, suffix: '只' },
  { label: '在线志愿者', value: 342, suffix: '人' },
  { label: '合作医院', value: 28, suffix: '家' },
]

const pets = [
  {
    name: '姜糖',
    age: '约 8 个月',
    city: '杭州市 · 西湖区',
    summary: '亲人、爱撒娇，已完成体检与首针疫苗。',
    status: '可预约见面',
    cover: 'linear-gradient(135deg, #ffd6ad 0%, #ffe8cf 100%)',
  },
  {
    name: '豆包',
    age: '约 2 岁',
    city: '杭州市 · 拱墅区',
    summary: '稳定安静，适合有陪伴时间的家庭。',
    status: '等待领养',
    cover: 'linear-gradient(135deg, #bce6dc 0%, #e8f8f3 100%)',
  },
  {
    name: '小杏仁',
    age: '约 1 岁',
    city: '杭州市 · 滨江区',
    summary: '活泼聪明，喜欢玩具球，社交能力强。',
    status: '热门推荐',
    cover: 'linear-gradient(135deg, #fddbb8 0%, #fff4e8 100%)',
  },
  {
    name: '奶盖',
    age: '约 3 岁',
    city: '杭州市 · 上城区',
    summary: '温柔黏人，已绝育，适合新手领养人。',
    status: '可视频看宠',
    cover: 'linear-gradient(135deg, #cfe9e4 0%, #f4fbf9 100%)',
  },
]

const activities = [
  {
    date: '2026-03-01',
    title: '周末流浪犬义诊专场',
    location: '城北爱心驿站',
    summary: '兽医团队将现场完成基础检查、驱虫评估与登记建档，支持志愿者协同引导。',
    type: '线下活动',
    cover: 'https://images.pexels.com/photos/6568940/pexels-photo-6568940.jpeg?auto=compress&cs=tinysrgb&w=1200',
  },
  {
    date: '2026-03-05',
    title: '社区夜间巡护联动',
    location: '滨江区四个巡护点',
    summary: '联合社区与志愿者开展夜间巡查，重点排查伤病与幼崽，实时回传定位信息。',
    type: '巡护行动',
    cover: 'https://images.pexels.com/photos/5732474/pexels-photo-5732474.jpeg?auto=compress&cs=tinysrgb&w=1200',
  },
  {
    date: '2026-03-09',
    title: '春季领养开放日',
    location: '西湖区救助中心',
    summary: '开放 30+ 只健康待领养毛孩子，现场提供领养评估、喂养建议与适应期指导。',
    type: '领养活动',
    cover: 'https://images.pexels.com/photos/4587991/pexels-photo-4587991.jpeg?auto=compress&cs=tinysrgb&w=1200',
  },
]

const articles = [
  {
    category: '公益宣传',
    title: '遇到受伤流浪宠物时，普通人第一步该做什么？',
    desc: '从环境安全判断、远距离观察到上报信息填写，整理成一份可执行的三步指南。',
    date: '2026-02-21',
    reading: '5 分钟阅读',
    icon: 'mdi:bullhorn-variant-outline',
    cover: 'https://picsum.photos/seed/pet-article-01/960/540',
    source: 'Picsum 占位图',
  },
  {
    category: '科普知识',
    title: '新手领养准备清单：疫苗、驱虫和家庭环境一次讲清',
    desc: '涵盖健康档案核验、居家防护改造、食谱过渡与应激应对，帮助领养更平稳。',
    date: '2026-02-19',
    reading: '8 分钟阅读',
    icon: 'mdi:book-open-page-variant-outline',
    cover: 'https://picsum.photos/seed/pet-article-02/960/540',
    source: 'Picsum 占位图',
  },
  {
    category: '公益宣传',
    title: '社区共建案例：一条街如何从高频救助到稳定管理',
    desc: '通过绝育、免疫、回访和科普四条线协同，三个月内显著降低了重复救助数量。',
    date: '2026-02-16',
    reading: '6 分钟阅读',
    icon: 'mdi:heart-circle-outline',
    cover: 'https://picsum.photos/seed/pet-article-03/960/540',
    source: 'Picsum 占位图',
  },
]

function scrollToSection(id) {
  const target = document.getElementById(id)
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}
</script>

<template>
  <div class="home-page">
    <AppHeader :nav-items="navItems" @navigate="scrollToSection" />

    <main>
      <HeroSection @navigate="scrollToSection" />
      <StatsOverview :stats="stats" />
      <AdoptionSection :pets="pets" />
      <ActivitySection :activities="activities" />
      <FeaturedArticlesSection :articles="articles" />
    </main>

    <AppFooter />
  </div>
</template>
