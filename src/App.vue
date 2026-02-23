<script setup>
import { ref } from 'vue'
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
  { id: 'news', label: '最新动态' },
  { id: 'articles', label: '公益科普' },
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

const updates = [
  {
    date: '2026-02-20',
    title: '“暖冬巡护”行动完成',
    desc: '本周新增救助 37 只，全部完成初筛与基础驱虫。',
  },
  {
    date: '2026-02-18',
    title: '周末领养开放日报名中',
    desc: '周六 14:00 在城北领养中心举行，支持线上预约到场。',
  },
  {
    date: '2026-02-15',
    title: '新增夜间急救值班机制',
    desc: '每日 20:00-24:00 开通夜间应急响应，缩短黄金救助时间。',
  },
]

const articles = [
  {
    category: '公益宣传',
    title: '如何正确帮助街头流浪猫狗：先观察，再行动',
    desc: '发现疑似受伤流浪宠物时，先判断环境安全并拍照留存，再通过平台快速上报位置与状态。',
    date: '2026-02-21',
    reading: '5 分钟阅读',
    icon: Bell,
  },
  {
    category: '科普知识',
    title: '领养前准备清单：新手家庭必做的 7 件事',
    desc: '包含疫苗档案确认、家庭防逃逸改造、饮食过渡与应激管理，帮助宠物平稳融入新家庭。',
    date: '2026-02-19',
    reading: '8 分钟阅读',
    icon: DataLine,
  },
  {
    category: '公益行动',
    title: '为什么“绝育 + 免疫 + 回访”是长期救助关键',
    desc: '系统化绝育和免疫能显著降低流浪数量增长，同时通过回访机制提升救助成功率。',
    date: '2026-02-16',
    reading: '6 分钟阅读',
    icon: Present,
  },
  {
    category: '科普知识',
    title: '冬季救助要点：低温环境下的紧急保暖方案',
    desc: '纸箱隔风、热源距离控制、湿度管理与临时补液等实操建议，适用于社区志愿者快速处置。',
    date: '2026-02-13',
    reading: '4 分钟阅读',
    icon: StarFilled,
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
          <p>已完成基础体检与评估，可在线预约见面。</p>
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

      <section id="news" class="content-section news-grid">
        <div>
          <div class="section-head left">
            <h2>最新动态</h2>
            <p>公开透明，确保每条救助线索可跟进。</p>
          </div>
          <el-card class="timeline-card" shadow="never">
            <el-timeline>
              <el-timeline-item
                v-for="item in updates"
                :key="item.date + item.title"
                :timestamp="item.date"
                type="warning"
              >
                <h4>{{ item.title }}</h4>
                <p>{{ item.desc }}</p>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </div>

        <div id="articles">
          <div class="section-head left">
            <h2>最新文章</h2>
            <p>传播科学救助知识，提升公众参与率。</p>
          </div>
          <div class="article-list">
            <el-card v-for="article in articles" :key="article.title" class="article-card" shadow="hover">
              <div class="article-head">
                <el-tag type="warning" effect="plain">{{ article.category }}</el-tag>
                <span>{{ article.date }}</span>
              </div>
              <h4>{{ article.title }}</h4>
              <p>{{ article.desc }}</p>
              <div class="article-foot">
                <span class="article-read">
                  <el-icon><component :is="article.icon" /></el-icon>
                  {{ article.reading }}
                </span>
                <el-button text type="warning" class="card-link">
                  阅读全文
                  <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </el-card>
          </div>
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
