<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
import {
  ArrowRight,
  Bell,
  Calendar,
  DataLine,
  House,
  LocationInformation,
  Medal,
  Pointer,
  Present,
  Search,
  StarFilled,
  User,
} from '@element-plus/icons-vue'

const menuOpen = ref(false)

const navItems = [
  { id: 'actions', label: '救助行动' },
  { id: 'adoption', label: '领养推荐' },
  { id: 'news', label: '近期活动' },
  { id: 'articles', label: '站内文章' },
]

const actionCards = [
  {
    title: '在线紧急报案',
    desc: '上传位置、照片与伤情描述，值班救助员会就近响应。',
    icon: Search,
    action: '立即上报',
  },
  {
    title: '爱心捐助通道',
    desc: '支持医疗、绝育与粮食补给，让每只毛孩子得到稳定照料。',
    icon: Present,
    action: '去捐助',
  },
  {
    title: '志愿者报名',
    desc: '加入巡查、临时寄养、线下领养日，和我们一起守护城市生命。',
    icon: User,
    action: '加入团队',
  },
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
  menuOpen.value = false
}
</script>

<template>
  <div class="home-page">
    <header class="site-header">
      <div class="nav-card">
        <div class="brand">
          <span class="brand-mark" aria-hidden="true">
            <el-icon><House /></el-icon>
          </span>
          <div class="brand-text">
            <strong>暖窝救助</strong>
            <small><span class="top-strip-right">24h 救助热线：400-820-1314</span></small>
          </div>
        </div>

        <nav class="desktop-nav" aria-label="主导航">
          <button
            v-for="item in navItems"
            :key="item.id"
            class="nav-link"
            type="button"
            @click="scrollToSection(item.id)"
          >
            {{ item.label }}
          </button>
        </nav>

        <div class="nav-action">
          <el-button class="nav-btn" type="warning" @click="scrollToSection('actions')">
            立即参与
          </el-button>
          <button
            class="mobile-toggle"
            type="button"
            aria-label="切换导航菜单"
            aria-controls="mobile-menu"
            :aria-expanded="menuOpen ? 'true' : 'false'"
            @click="menuOpen = !menuOpen"
          >
            <el-icon><Pointer /></el-icon>
          </button>
        </div>
      </div>

      <transition name="menu-fade">
        <nav v-if="menuOpen" id="mobile-menu" class="mobile-menu" aria-label="移动端导航">
          <button
            v-for="item in navItems"
            :key="item.id"
            class="mobile-link"
            type="button"
            @click="scrollToSection(item.id)"
          >
            {{ item.label }}
          </button>
        </nav>
      </transition>
    </header>

    <main>
      <section class="hero-section" id="home">
        <div class="hero-copy">
          <el-tag class="hero-tag" type="warning" effect="plain">城市联合救助计划</el-tag>
          <h1>让每一次相遇，都成为被温柔接住的开始</h1>
          <p>
            连接居民、志愿者、医院与公益组织，建立从“发现”到“救助”再到“领养”的完整闭环。
            每一条上报都会被记录、追踪、反馈。
          </p>
          <div class="hero-buttons">
            <el-button class="warm-btn" type="warning" size="large" @click="scrollToSection('adoption')">
              查看领养专区
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button class="soft-btn" size="large" @click="scrollToSection('actions')">
              我要参与救助
            </el-button>
          </div>
          <ul class="hero-list">
            <li><el-icon><LocationInformation /></el-icon> 覆盖 12 个城区联动响应</li>
            <li><el-icon><Bell /></el-icon> 重要事件实时通知与回访</li>
            <li><el-icon><DataLine /></el-icon> 救助数据全流程可追溯</li>
          </ul>
        </div>

        <div class="hero-panel">
          <el-card class="progress-card" shadow="never">
            <div class="panel-head">
              <h3>本周救助进度</h3>
              <el-tag type="success">进行中</el-tag>
            </div>
            <div class="progress-metrics">
              <div>
                <span>新接收</span>
                <strong>42</strong>
              </div>
              <div>
                <span>医疗处理</span>
                <strong>29</strong>
              </div>
              <div>
                <span>完成安置</span>
                <strong>17</strong>
              </div>
            </div>
            <el-progress :stroke-width="12" :percentage="78" color="#e77a3b" />
            <p class="panel-note">距离本周目标还差 13 只，正在持续接力。</p>
          </el-card>

          <el-card class="story-card" shadow="hover">
            <div class="story-head">
              <el-icon><StarFilled /></el-icon>
              <span>今日暖心故事</span>
            </div>
            <p>
              志愿者在地铁口发现受伤的“奶盖”，3 小时内完成转运、检查与临时安置。
              目前恢复情况稳定，已开放领养申请。
            </p>
          </el-card>
        </div>
      </section>

      <section class="stats-bar" aria-label="救助数据概览">
        <el-row :gutter="16">
          <el-col v-for="item in stats" :key="item.label" :xs="12" :sm="6">
            <el-card class="stat-card" shadow="never">
              <el-statistic :value="item.value">
                <template #title>{{ item.label }}</template>
                <template #suffix>{{ item.suffix }}</template>
              </el-statistic>
            </el-card>
          </el-col>
        </el-row>
      </section>

      <section id="adoption" class="content-section">
        <div class="section-head">
          <h2>领养推荐</h2>
        </div>
        <div class="pet-grid">
          <article v-for="pet in pets" :key="pet.name">
            <el-card class="pet-card" shadow="hover">
              <div class="pet-cover" :style="{ background: pet.cover }">
                <el-tag type="warning" effect="dark" size="small">{{ pet.status }}</el-tag>
              </div>
              <div class="pet-body">
                <h3>{{ pet.name }}</h3>
                <p>{{ pet.summary }}</p>
                <div class="pet-meta">
                  <span><el-icon><Calendar /></el-icon>{{ pet.age }}</span>
                  <span><el-icon><LocationInformation /></el-icon>{{ pet.city }}</span>
                </div>
                <el-button text type="warning" class="card-link">
                  预约见面
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-card>
          </article>
        </div>
      </section>

      <section id="news" class="content-section">
        <div class="section-head left">
          <h2>近期活动</h2>
          <p>近期活动横向展示，支持快速了解时间、地点与主题。</p>
        </div>
        <div class="activity-row">
          <article v-for="item in activities" :key="item.title">
            <el-card class="activity-card" shadow="hover">
              <div class="activity-cover">
                <img :src="item.cover" :alt="item.title" loading="lazy" />
                <el-tag class="activity-tag" type="warning" effect="dark" size="small">{{ item.type }}</el-tag>
              </div>
              <div class="activity-body">
                <h4>{{ item.title }}</h4>
                <p>{{ item.summary }}</p>
                <div class="activity-meta">
                  <span>
                    <Icon icon="mdi:calendar-clock-outline" />
                    {{ item.date }}
                  </span>
                  <span>
                    <Icon icon="mdi:map-marker-radius-outline" />
                    {{ item.location }}
                  </span>
                </div>
                <el-button text type="warning" class="card-link">
                  查看活动详情
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-card>
          </article>
        </div>
      </section>

      <section id="articles" class="content-section">
        <div class="section-head left">
          <h2>站内文章</h2>
          <p>公益宣传与科普知识分开展示，阅读入口更清晰。</p>
        </div>
        <div class="article-row">
          <article v-for="article in articles" :key="article.title">
            <el-card class="article-card" shadow="hover">
              <div class="article-cover">
                <img :src="article.cover" :alt="article.title" loading="lazy" />
              </div>
              <div class="article-body">
                <div class="article-head">
                  <el-tag type="warning" effect="plain">{{ article.category }}</el-tag>
                  <span>{{ article.date }}</span>
                </div>
                <h4>{{ article.title }}</h4>
                <p>{{ article.desc }}</p>
                <div class="article-foot">
                  <span class="article-read">
                    <Icon :icon="article.icon" />
                    {{ article.reading }}
                  </span>
                  <el-button text type="warning" class="card-link">
                    阅读全文
                    <el-icon><ArrowRight /></el-icon>
                  </el-button>
                </div>
                <div class="article-source">
                  <Icon icon="mdi:image-outline" />
                  {{ article.source }}
                </div>
              </div>
            </el-card>
          </article>
        </div>
      </section>

      <section id="actions" class="content-section">
        <div class="section-head">
          <h2>你可以这样参与救助</h2>
          <p>三个入口，覆盖上报、捐助、行动报名。</p>
        </div>
        <el-row :gutter="20">
          <el-col v-for="item in actionCards" :key="item.title" :xs="24" :sm="12" :lg="8">
            <el-card class="action-card" shadow="hover">
              <el-icon class="action-icon"><component :is="item.icon" /></el-icon>
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
              <el-button class="card-link" text type="warning">
                {{ item.action }}
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </section>
    </main>

    <footer class="site-footer">
      <div class="footer-left">
        <h3>暖窝救助 · Stray Pet Rescue</h3>
        <p>让更多流浪生命被看见、被治疗、被爱护、被领养。</p>
      </div>
      <div class="footer-right">
        <span><el-icon><Medal /></el-icon> 公益透明公示</span>
        <span><el-icon><Present /></el-icon> 爱心物资直达站点</span>
      </div>
    </footer>
  </div>
</template>
