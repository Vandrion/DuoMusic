@file:OptIn(ExperimentalMaterial3Api::class)

package com.maxrave.simpmusic.ui.screen.player

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable as FloatAnimatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.maxrave.simpmusic.expect.HapticFeedback
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.cloud_comments_unavailable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kmpalette.rememberPaletteState
import com.maxrave.common.Config.MAIN_PLAYER
import com.maxrave.common.MERGING_DATA_TYPE
import com.maxrave.domain.extension.isVideo
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.expect.ui.PlatformBackHandler
import com.maxrave.simpmusic.expect.ui.rememberVideoAspectRatio
import com.maxrave.simpmusic.extension.GradientAngle
import com.maxrave.simpmusic.extension.GradientOffset
import com.maxrave.simpmusic.extension.KeepScreenOn
import com.maxrave.simpmusic.expect.hapticTapFeedback
import com.maxrave.simpmusic.extension.getColorFromPalette
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.extension.hsvToColor
import com.maxrave.simpmusic.extension.parseTimestampToMilliseconds
import com.maxrave.simpmusic.extension.rememberIsInPipMode
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.AddLyricsSheet
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.FullscreenLyricsContent
import com.maxrave.simpmusic.ui.component.FullscreenLyricsSheet
import com.maxrave.simpmusic.ui.component.CommentsBottomSheet
import com.maxrave.simpmusic.ui.component.InfoPlayerBottomSheet
import com.maxrave.simpmusic.ui.component.NeteaseCommentsSheet
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.utils.YouTubeLikeStatus
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.QueueBottomSheet
import com.maxrave.simpmusic.ui.component.queueRowIsCloudDisk
import com.maxrave.simpmusic.ui.component.QueuePosition
import com.maxrave.simpmusic.ui.component.VoteLyricsDialog
import com.maxrave.simpmusic.ui.component.YouTubeDislikeState
import com.maxrave.simpmusic.ui.icon.KeyboardArrowDown
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.home.ListenTogetherDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.login.LoginDestination
import com.maxrave.simpmusic.ui.navigation.destination.player.FullscreenDestination
import com.maxrave.common.NETEASE_CLOUD_DISK_PLAYLIST_ID
import com.maxrave.common.NETEASE_MV_MEDIA_ID_PREFIX
import com.maxrave.simpmusic.ui.screen.player.content.ArtworkPagerSnapAnimation
import com.maxrave.simpmusic.ui.screen.player.content.NowPlayingContentActions
import com.maxrave.simpmusic.ui.screen.player.content.NowPlayingContentAppleMusic
import com.maxrave.simpmusic.ui.screen.player.content.NowPlayingContentSoundCloud
import com.maxrave.simpmusic.ui.screen.player.content.NowPlayingContentM3Expressive
import com.maxrave.simpmusic.ui.screen.player.content.NowPlayingContentSpotify
import com.maxrave.simpmusic.ui.screen.player.content.NowPlayingContentState
import com.maxrave.simpmusic.ui.screen.player.content.PlayerBackdropColor
import com.maxrave.simpmusic.ui.screen.player.content.toAudioCodecLabel
import com.maxrave.simpmusic.ui.screen.player.content.fullscreenCanvas
import com.maxrave.simpmusic.ui.screen.player.content.toAudioQualityLabel
import com.maxrave.simpmusic.viewModel.LyricsProvider
import com.maxrave.simpmusic.viewModel.NowPlayingBottomSheetUIEvent
import com.maxrave.simpmusic.viewModel.NowPlayingBottomSheetViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.TimeSource
import kotlin.time.Duration.Companion.milliseconds
import org.koin.compose.koinInject
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import com.maxrave.simpmusic.ui.component.rememberSurfaceDarkColors
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.LibraryMutation
import com.maxrave.simpmusic.viewModel.LibraryMutationBus
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.cloud_delete_done
import simpmusic.composeapp.generated.resources.delete
import simpmusic.composeapp.generated.resources.delete_from_cloud_disk_title
import simpmusic.composeapp.generated.resources.delete_from_cloud_disk_message
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.math.max

// Desktop's fullscreen lyrics page: in a little slower than out, so opening reads as arriving and
// closing gets out of the way.
private const val FULLSCREEN_LYRICS_ENTER_MS = 300
private const val FULLSCREEN_LYRICS_EXIT_MS = 220

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalMaterial3Api
@Composable
fun NowPlayingScreen(
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
    onDismiss: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    // 内容真实高度用 onSizeChanged 量——LocalWindowInfo.containerSize 在 ModalBottomSheet 的
    // popup 窗口里拿到的是错的(实测 ~0)。
    var contentHeightPx by remember { mutableIntStateOf(0) }
    // 程序化关页标记:关页按钮走的 hideSheet() 置位,confirmValueChange 对它放行——
    // 否则 offset≈0 的程序化关闭会被"位移不足"裁决误拦。
    var programmaticHide by remember { mutableStateOf(false) }
    // 最近一次手势结束时的纵向速度(px/s,Final 通道采样):confirmValueChange 的 flick
    // 放行判据用——见下方否决线注释(写在这里是回调读取,快照写不触发重组)。
    var lastGestureVelocityY by remember { mutableFloatStateOf(0f) }
    // flick 放行速度阈值(dp/s):≈库 AnchoredDraggable 速度阈值(125dp/s)的 2.2 倍,
    // 真 flick 远高于它,指尖抖动/慢拉远低于它,分界清晰。
    val dismissFlickVelocityPx =
        with(LocalDensity.current) { SHEET_DISMISS_FLICK_VELOCITY.dp.toPx() }
    // 内容 Box 在窗口里的位置(逐帧刷新):速度采样把 change.position(局部)加回它
    // 得到窗口系指针位置——原生下拉时 sheet 跟手 1:1 平移,局部坐标速度恒为噪声。
    // 只在指针回调里读,逐帧写入不触发重组。
    var contentBoxPosInWindow by remember { mutableStateOf(Offset.Zero) }
    // 下拉激活(拖动跟手中):拖动期间冻结重效果层(iOS 控制中心同款降级渲染——七轮封面
    // "质量锁"同族手法)。锁外=sheet offset 流;锁窗内 sheet 不动,由 fallback 回调补。
    var pullActive by remember { mutableStateOf(false) }
    // 锁窗内 fallback 的跟手位移(px):下拉进度=它与 sheet offset 取大——锁窗内 sheet 不动
    // (offset 恒 0),进度由 fallback 补;稳态由 offset 驱动。遮罩渐隐与顶部渐变圆角共用。
    var fallbackPullPx by remember { mutableFloatStateOf(0f) }
    // 锁窗内上滑→列表滚动转发器(Content 拿到 mainScrollState 后注册)
    var upDragSink by remember { mutableStateOf<((Float) -> Unit)?>(null) }
    var sheetStateRef by remember { mutableStateOf<SheetState?>(null) }
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            // (fix) 下滑关闭裁决——confirmValueChange=库的官方拦截点。否决线**必须速度感知**
            // (2026-11-17"开页 ~1s 下滑失灵"定案):库 AnchoredDraggable 的关页判定=位移过
            // 半屏(位置规则)**或**释放速度超 125dp/s(速度规则,与拉距无关)——慢拉永远在
            // 中点以下就进不了 Hidden 判定,所以能走到本回调 Hidden 分支的"短位移"情形全是
            // **快 flick**:纯位移 8% 线会把这种最自然的"快速轻扫关页"整类拦死=用户体感的
            // "下滑手势失灵,再划一次才行"(实测复现:600px/s 短 flick 被拦零响应)。改为
            // iOS 双条件:位移 ≥8%(慢拉有意收尾)或 |速度| ≥flick 阈值(有意轻扫)都放行,
            // 只拦"又慢又短"的真误触(指尖搭着慢慢蹭 <8% 且 <280dp/s)。程序化关闭一律放行。
            confirmValueChange = { target ->
                if (target != SheetValue.Hidden) {
                    true
                } else {
                    val h = contentHeightPx
                    val off = sheetStateRef?.requireOffset() ?: 0f
                    val allow =
                        programmaticHide || h <= 0 ||
                            off >= h * SHEET_DISMISS_MIN_FRACTION ||
                            (off >= h * SHEET_DISMISS_FLICK_MIN_FRACTION &&
                                abs(lastGestureVelocityY) > dismissFlickVelocityPx)
                    allow
                }
            },
        )
    sheetStateRef = sheetState
    // 锁外下拉=sheet offset 流驱动;锁窗内 sheet 不动,由 fallback 的 onPullActive 补
    LaunchedEffect(sheetState) {
        snapshotFlow {
            try {
                sheetState.requireOffset()
            } catch (e: IllegalStateException) {
                0f
            }
        }.collect { pullActive = it > 8f }
    }
    // Fork(2026-11-16"背景灰透,拉一半才透明"):m3 的 scrim alpha 是 animateFloatAsState(
    // sheetState.isVisible),而 isVisible=currentValue!=Hidden——拖动中途不随 offset 淡出,
    // 露出的底页恒被整层黑罩着,过阈/落定才瞬变渐隐(观感="灰透、拉过半才突然透明")。
    // 改成 offset 驱动:遮罩透明度随下拉距离 1:1 线性渐隐(iOS 关页同款);快甩/BACK 程序化
    // 关闭同样随关页动画渐隐到底,弹回 Expanded 渐回。Scrim 内部 drawRect(color, alpha=库的
    // 动画×color.alpha),颜色自身的 alpha 可用。
    val scrimPullFraction by remember(sheetState) {
        derivedStateOf {
            val h = contentHeightPx
            if (h <= 0) {
                0f
            } else {
                // 锁窗内 sheet 不动(offset 恒 0),进度必须由 fallback 位移补——漏掉它,
                // "开页立即下拉关页"的整条手势遮罩恒灰透无渐变(2026-11-16 用户复测抓到)。
                max(runCatching { sheetState.requireOffset() }.getOrDefault(0f), fallbackPullPx) / h
            }
        }
    }

    val hideSheet: () -> Unit = {
        coroutineScope.launch {
            programmaticHide = true
            sheetState.hide()
            onDismiss()
        }
    }


    // Fork(2026-11-16"开页顶部上下颤抖"):sheet 的开/关/拖拽动画 spec 全部取自组合里的
    // MaterialTheme.motionScheme(BottomSheetKt setShowMotionSpec←defaultSpatialSpec 字节码
    // 在案),而 app 根主题是 MaterialExpressiveTheme——expressive 的 spatial spring 自带
    // 过冲,sheet 开页到顶后整面回弹 ~0.45s(逐帧实测顶部特征 600→537→573 震荡落定)。
    // 渐变/大图类主题被满屏内容掩盖,SoundCloud 的黑标签条/圆钮硬边把它放大成明显的
    // "上下颤抖"。只给 sheet 子树换 standard motion scheme(临界阻尼,平缓到位):
    // 开/关/拖拽 settle 全部收敛;内容里真正带 expressive 动效的主题(M3E/SoundCloud)
    // 本来就在自己内部重建 MaterialExpressiveTheme,不受影响。
    MaterialExpressiveTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = typo(),
        motionScheme = MotionScheme.standard(),
    ) {
        ModalBottomSheet(
        modifier =
            Modifier
                .fillMaxHeight(),
        onDismissRequest = {
            onDismiss()
        },
        // Fork(用户 2026-11-15):容器透明——下拉跟手时内容层移走,露出的是 scrim 遮罩下的
        // 真实主页(与原生 sheet 下拉一致);纯黑/垫色都会在浅色主题下突兀(真机录像实证
        // 顶部灰白条)。正常态各主题内容自带满屏背景,透明容器不可见。
        containerColor = Color.Transparent,
        dragHandle = {},
        // 遮罩透明度走"前段归零+量化"曲线(见 SHEET_SCRIM_FADE_END / SHEET_SCRIM_ALPHA_STEPS
        // 常量处注释):derived 值量化后只在步进边界变化,拖动中本 scope 的重组从逐帧降到
        // 十几次;顶部渐变圆角仍在 graphicsLayer 里按未量化 frac 逐帧画(draw 层零重组)。
        scrimColor =
            Color.Black.copy(
                alpha = SHEET_SCRIM_BASE_ALPHA * scrimFadeAlpha(scrimPullFraction),
            ),
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        shape = RectangleShape,
    ) {
        // BACK 关页自己接(handler 须在 popup 内容内层注册才赢过 sheet 内部 back;外层窗口
        // 的 handler 收不到 popup 的 back):库的 back→Hidden 路径同样过 confirmValueChange,
        // offset=0 会被"位移不足"裁决误拦——接管后走 hideSheet(带程序化标记)带动画自然关。
        PlatformBackHandler(enabled = true) {
            hideSheet()
        }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .onSizeChanged { contentHeightPx = it.height }
                    .onGloballyPositioned { contentBoxPosInWindow = it.positionInWindow() }
                    // (fix) 跨线预告震感(Final 通道旁观,不消费任何事件):拖动中手势进入
                    // "松手即关"状态时给一次震感(iOS 式预告,armed 单次,回落重置)。判据与
                    // confirmValueChange 的放行线同构(位置 8% 线 OR 速度+4% 门槛)——快扫在
                    // 4%-8% 区间即可触发关闭,预告线只挂 8% 会让该区间零预告直接关。同层顺带
                    // 采样手势纵向速度(confirmValueChange 的 flick 放行判据用;settle 决策
                    // 早于 up 事件的 Final 通道,所以每个 move 都实时算一次写入,up 时少采
                    // 这一个点不影响速度)。🚨采样必须用窗口坐标(局部 position+contentBox
                    // PosInWindow):原生下拉时 sheet 表面跟手 1:1 平移,指针的局部坐标位移
                    // ≈0,局部速度恒为噪声(实测 -137px/s,方向都是反的,探针在 confirm
                    // 决策瞬间抓到的真实读数 off=122/vel=-137)。
                    .pointerInput(sheetState, contentHeightPx) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val velocityTracker = VelocityTracker()
                            velocityTracker.resetTracking()
                            // 新手势开始即清零:confirm 决策若发生在本手势采到首个有效速度
                            // 之前(极快 flick 的 settle 可能先于第二个 move),读到上一手势
                            // 的残留速度=本应拦下的短慢拖被旧速度放行
                            lastGestureVelocityY = 0f
                            var hapticArmed = true
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Final)
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                velocityTracker.addPosition(change.uptimeMillis, change.position + contentBoxPosInWindow)
                                // 每个 move 实时写:confirmValueChange 在 settle 决策期被调,
                                // 早于本 up 事件的 Final 通道——结束时才写会拿到上一个手势的
                                // 旧速度(首轮=0,flick 永远被旧值拦掉,实测踩过)
                                runCatching {
                                    lastGestureVelocityY = velocityTracker.calculateVelocity().y
                                }
                                val h = contentHeightPx
                                if (h > 0) {
                                    val off =
                                        try {
                                            sheetState.requireOffset()
                                        } catch (e: IllegalStateException) {
                                            null
                                        }
                                    if (off != null) {
                                        // 预告线与关闭判据同构:位置越过 8% 线,或速度已达
                                        // flick 放行线且位移过 4% 门槛(该手势松手必关)。
                                        // 快扫在 4%-8% 区间就能触发关闭,预告必须同样前移,
                                        // 否则该区间直接关页零预告。
                                        val willDismissByPosition = off >= h * SHEET_DISMISS_MIN_FRACTION
                                        val willDismissByFlick =
                                            off >= h * SHEET_DISMISS_FLICK_MIN_FRACTION &&
                                                abs(lastGestureVelocityY) > dismissFlickVelocityPx
                                        if (hapticArmed && (willDismissByPosition || willDismissByFlick)) {
                                            hapticArmed = false
                                            HapticFeedback.tap()
                                        } else if (!hapticArmed && !willDismissByPosition && !willDismissByFlick) {
                                            hapticArmed = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // (fix) 打开动画未稳住的 ~0.6-1s 里 AnchoredDraggable 的拖拽互斥锁被
                    // animateTo 持有,下滑手势被库整体吞掉(Google issuetracker #481364105 同族,
                    // 2026-10-03 实测确认)。锁窗内旁观未消费向下位移,过阈值直接 hideSheet()
                    // (排队等锁后自然关页,带动画)。仅此窗存在,稳态手势完全由库自己处理。
                    .earlySheetDismissFallback(
                        sheetState = sheetState,
                        onHide = { programmaticHide = true; hideSheet() },
                        onGone = { onDismiss() },
                        onPullActive = { active -> pullActive = active },
                        onPullProgress = { px -> fallbackPullPx = px },
                        onUpDrag = upDragSink,
                    )
                    // Fork(2026-11-16"下拉关页顶部是直角"):渐变圆角——顶部 R 角随下拉距离
                    // 线性长大(0→SHEET_DISMISS_TOP_CORNER),iOS 关页观感;弹回即收回。进度=
                    // sheet offset 与锁窗内 fallback 位移取大(都从 snapshot state 读,逐帧不落
                    // 重组)。必须在 earlySheetDismissFallback 之后挂:它的 translationY 层在
                    // 本层之外,圆角才跟着内容走;放前面会在屏幕原位裁出一个不动的圆角"洞"。
                    // 容器透明+offset 驱动遮罩(见上),圆角露出处即渐亮的底页。
                    .graphicsLayer {
                        val h = contentHeightPx
                        val frac =
                            if (h <= 0) {
                                0f
                            } else {
                                val off = runCatching { sheetState.requireOffset() }.getOrDefault(0f)
                                max(off, fallbackPullPx) / h
                            }.coerceIn(0f, 1f)
                        val corner = SHEET_DISMISS_TOP_CORNER * frac
                        shape = RoundedCornerShape(topStart = corner, topEnd = corner)
                        clip = true
                    },
        ) {
            NowPlayingScreenContent(
                isPullingDown = pullActive,
                onUpDragSinkReady = { upDragSink = it },
                sharedViewModel = sharedViewModel,
                navController = navController,
                isExpanded = sheetState.currentValue == SheetValue.Expanded,
                dismissIcon = SimpIcons.KeyboardArrowDown,
                onDismiss = {
                    hideSheet()
                },
            )
        }
    }
    }
}

/**
 * (fix) 打开动画未稳住时的下滑兜底——锁窗内内容层自己跟手,锁一释放立即交还库层。
 *
 * material3(JetBrains 1.12)ModalBottomSheet 的打开动画仍在跑时,AnchoredDraggable 的拖拽
 * 互斥锁被 animateTo 持有,用户下滑被整体吞掉约 0.6~1s(Google issuetracker #481364105
 * 同族;真机体感="第一次下滑不跟手,再滑一次才跟手"——第一次撞锁窗,第二次锁已释放)。
 *
 * 锁窗内(动画仍在跑,或打开未满 [SHEET_DISMISS_LOCK_WINDOW]):库拖不动 sheet,内容层用
 * translationY 1:1 跟手(消费位移,防锁中途释放后库双重接管);松手按同一关闭线裁决:
 * 过 [SHEET_DISMISS_FRACTION] 内容层自己滑出屏+直接出栈,否则 spring 弹回;拖动中锁释放
 * 也弹回退位。锁窗外(手势开始时锁已释放)完全不干预——稳态的跟手/弹回/关闭全由库处理,
 * dismiss 的位移/速度裁决在 confirmValueChange。
 */
@Composable
private fun Modifier.earlySheetDismissFallback(
    sheetState: SheetState,
    onHide: () -> Unit,
    onGone: () -> Unit,
    onPullActive: (Boolean) -> Unit = {},
    /** 锁窗内跟手位移(px,壳层拿它与 sheet offset 取大驱动遮罩渐隐/渐变圆角)。 */
    onPullProgress: (Float) -> Unit = {},
    /** 锁窗内上滑转发(经典/M3E 整页列表):dy<0 原样转发 dispatchRawDelta 真滚列表——
     *  库动画锁(~1s)阻塞一切嵌套分发,列表自己的手势也被吞,不转发=开页立即上滑失灵
     *  (真机实测 0.8s 仍失灵/1.2s 恢复)。null=主题无可滚列表,上滑交还。 */
    onUpDrag: ((Float) -> Unit)? = null,
): Modifier {
    val openedAt = remember { TimeSource.Monotonic.markNow() }
    val currentSheetState by rememberUpdatedState(sheetState)
    val currentHide by rememberUpdatedState(onHide)
    val currentGone by rememberUpdatedState(onGone)
    val currentPullActive by rememberUpdatedState(onPullActive)
    val currentPullProgress by rememberUpdatedState(onPullProgress)
    val currentUpDrag by rememberUpdatedState(onUpDrag)
    var contentHeightPx by remember { mutableIntStateOf(0) }
    // 锁窗内的跟手位移:直写 state(graphicsLayer 快照读,逐帧不落重组);动画段用一次性
    // FloatAnimatable 驱动 state——restricted pointerInput scope 里不能挂起,全部走 scope.launch
    var lockedPullY by remember { mutableFloatStateOf(0f) }
    // 上滑转发手势的速度采样(松手 fling 接力用;每次手势 down 时 reset)
    val velocityTracker = remember { VelocityTracker() }
    val scope = rememberCoroutineScope()
    fun animatePullTo(
        target: Float,
        spec: AnimationSpec<Float>,
        onEnd: () -> Unit = {},
    ) {
        scope.launch {
            FloatAnimatable(lockedPullY).animateTo(target, spec) {
                lockedPullY = value
                currentPullActive(value > 4f && target > 4f)
                currentPullProgress(value)
            }
            onEnd()
        }
    }
    return this
        .onSizeChanged { contentHeightPx = it.height }
        .graphicsLayer { translationY = lockedPullY }
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val pointerId = down.id
                var accY = 0f
                var pulling = false
                var scrollForwarding = false
                var sumY = 0f
                velocityTracker.resetTracking()
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                    if (!change.pressed) {
                        // 松手:过线滑出+出栈,否则弹回;上滑转发手势松手=按采到速度
                        // 衰减惯性滚(原生 scrollable 已被中途消费饿死,没人替它 fling)。
                        // 关闭线裁决到这一刻才读 contentH——首帧手势(contentH 尚未
                        // 量出)此前在循环外直接 return 弃权,整条手势落入库层
                        // anchoredDraggable,"开页立即下拉 250px+释放速度"会被库 fling
                        // 到 Hidden=播放页直接消失(模拟器实测复现);位移跟踪本身
                        // 不依赖高度,只有这条阈值线依赖,此时 Box 早已量完。
                        if (pulling) {
                            val contentH = contentHeightPx
                            if (contentH > 0 && accY >= contentH * SHEET_DISMISS_FRACTION) {
                                HapticFeedback.tap()
                                animatePullTo(contentH.toFloat(), tween(200, easing = LinearOutSlowInEasing)) { currentGone() }
                            } else {
                                animatePullTo(0f, spring(dampingRatio = 0.75f, stiffness = 380f))
                            }
                        } else if (scrollForwarding) {
                            val v = velocityTracker.calculateVelocity().y
                            if (abs(v) > 100f) {
                                scope.launch {
                                    var last = 0f
                                    AnimationState(initialValue = 0f, initialVelocity = v).animateDecay(exponentialDecay()) {
                                        val delta = value - last
                                        last = value
                                        currentUpDrag?.invoke(delta)
                                    }
                                }
                            }
                        }
                        break
                    }
                    // 只武装到锁窗结束(动画仍在跑,或打开未满 450ms——后者盖住动画尚未 START 的几帧)。
                    // 已在拉/已在转发滚动的手势中途锁释放不退位:位移一直被本观察器消费(库收不到,
                    // 无双动),继续跟手到松手统一裁决——中途退位会把已拉的距离丢掉(实测 500px 白拉
                    // 弹回;上滑转发同理:子层 scrollable 见过消费事件已自我取消,退位=滚动停在半途,
                    // "开页立即上滑画出一点歌词页后手势失灵,再划一次才行"的根因)。锁窗外新起的手势
                    // 由这里的起始检查退位,稳态交还库层。
                    if (!pulling && !scrollForwarding) {
                        val animating = currentSheetState.isAnimationRunning
                        val inWindow = openedAt.elapsedNow() < SHEET_DISMISS_LOCK_WINDOW
                        if (!animating && !inWindow) break
                    }
                    val dy = change.positionChange().y
                    if (!pulling && !scrollForwarding) {
                        // 方向仲裁:净位移过 touch slop 才定向(下=拉 sheet 关页,上=转发列表滚动)。
                        // 定向前不消费,锁窗内子层本就被库锁阻塞,窗内无副作用。
                        sumY += dy
                        if (abs(sumY) >= viewConfiguration.touchSlop) {
                            if (sumY > 0f) {
                                pulling = true
                                accY = sumY
                                change.consume()
                                lockedPullY = accY
                                currentPullActive(accY > 4f)
                                currentPullProgress(accY)
                            } else if (currentUpDrag != null) {
                                scrollForwarding = true
                            }
                            // else:上滑但 sink 还没注册(内容仍在合成,开页最初一两百毫秒)——
                            // 不 break!整条手势保持认领,等 sink 一就位即转转发。此前这里直接
                            // break 丢弃手势,观察器退出后注入的剩余位移无人接,页内容
                            // 纹丝不动(实测"开页立即上滑完全失灵"的第二窗口);列表主题
                            // 的 sink 注册只慢几帧,无列表主题留在这个未定向态也无副作用
                            // (不消费任何事件,松手自然结束)。
                        }
                    } else if (pulling) {
                        if (dy > 0f || accY > 0f) {
                            accY = (accY + dy).coerceAtLeast(0f)
                            // 锁窗内跟手:内容层位移 1:1(消费,防锁中途释放后库双重接管)
                            change.consume()
                            lockedPullY = accY
                            currentPullActive(accY > 4f)
                            currentPullProgress(accY)
                        }
                    } else if (scrollForwarding) {
                        // 双向转发(与原生 scroll 会话同语义:上滑下滚,回滑上滚),消费防
                        // 子层/库双重接管;顺带喂速度,松手 fling 接力
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        currentUpDrag?.invoke(dy)
                        change.consume()
                    }
                }
            }
        }
}

// 锁窗内自绘关闭线=内容高 15%(开页后快速下拉关页的顺手距离)。
private const val SHEET_DISMISS_FRACTION = 0.15f


// confirm veto 线=内容高 8%:库已决定去 Hidden 时,位移不足此值**且**释放速度低于
// flick 阈值才否决(速度感知双条件,见 confirmValueChange 注释)——拦的只是"又慢又短"
// 的指尖蹭滑;快 flick 无论拉距一律放行。
private const val SHEET_DISMISS_MIN_FRACTION = 0.08f

// flick 放行速度阈值(dp/s):库自身 125dp/s 就按速度关页,本阈值只决定我们对库已判
// 定关页的 flick 是否放行——取 180dp/s(≈1.4 倍库阈值):中等力度的扫动也放行,慢蹭
// (典型 <50dp/s)仍被拦。真正防"几十 px 快滑误关"旧投诉的是上面的 4% 位移门槛。
private const val SHEET_DISMISS_FLICK_VELOCITY = 180f

// 速度放行的**最低位移门槛**(内容高 4% ≈ 1.9cm):短扫必须"有一定位移+够快"两条件同时
// 成立才关页。历史双向校准:几十 px 的快滑关页=太敏感(用户长期投诉,"轻碰就整页关"),
// 150px 快扫不关=失灵(2026-11-17 报告)——4% 恰在两案之间:65px@800px/s 弹回(保旧),
// 130px@1250px/s 关页(修新)。adb 注入速度有运行间波动,阈值别贴着任何一案的实测值。
private const val SHEET_DISMISS_FLICK_MIN_FRACTION = 0.04f

// 下拉遮罩的静止态透明度(原恒定 .5f;现在下拉时按前段归零曲线从这里渐隐到 0)。
private const val SHEET_SCRIM_BASE_ALPHA = 0.5f

/**
 * 遮罩渐隐曲线(2026-11-17"一比一下拉顶部抖动"二轮优化):
 *
 * 1. **前段归零(0→[SHEET_SCRIM_FADE_END] 线性渐隐到 0,iOS 关页同款前段淡出)**——m3 的
 *    Scrim alpha 是 `animateFloatAsState(target = derivedStateOf { targetValue != Hidden },
 *    spec = MotionScheme spring)`(ModalBottomSheet.kt 源码在案),而拖动中 targetValue 随
 *    位置/速度**实时翻转**(过半屏→Hidden,拉回→Expanded):库 alpha 会跟着 spring 来回追,
 *    与我们的线性渐隐 alpha 相乘=半屏附近上下拉时遮罩忽明忽暗(用户报的"顶部抖动")。
 *    让我们的遮罩在翻转点(~50% 拉距)之前就归零,库 alpha 的振荡乘不到可见值。
 * 2. **量化到 [SHEET_SCRIM_ALPHA_STEPS] 步**——scrimColor 是组合期参数,逐帧变化会把
 *    NowPlayingScreen 整个 scope 拖进逐帧重组,拖动帧平白多一笔重组开销;量化后 derived
 *    值只在步进边界变化,重组次数从每帧一降到整程十几次(0.03/步的亮度差不可辨)。
 */
private const val SHEET_SCRIM_FADE_END = 0.45f
private const val SHEET_SCRIM_ALPHA_STEPS = 16

private fun scrimFadeAlpha(pullFraction: Float): Float {
    val frac = pullFraction.coerceIn(0f, 1f)
    val fade = (1f - frac / SHEET_SCRIM_FADE_END).coerceIn(0f, 1f)
    return (fade * SHEET_SCRIM_ALPHA_STEPS).toInt() / SHEET_SCRIM_ALPHA_STEPS.toFloat()
}

// 下拉关页顶部渐变圆角的上限(拉满一屏时),对齐 material sheet 默认圆角档。
private val SHEET_DISMISS_TOP_CORNER = 28.dp

// 打开后的动画锁窗:盖住"动画未启动"的几帧(动画本体 ~400ms 由 isAnimationRunning 覆盖)。
// 曾取 800ms——真机"开页立即上滑失灵第二次才行"反馈后收紧:库动画锁会阻塞嵌套滚动
// 分发,窗越长撞上滑/其它手势的面越大;450ms 是动画时长+边界帧的最小覆盖。
private val SHEET_DISMISS_LOCK_WINDOW = 450.milliseconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingScreenContent(
    isPullingDown: Boolean = false,
    onUpDragSinkReady: (((Float) -> Unit)?) -> Unit = {},
    sharedViewModel: SharedViewModel = koinInject(),
    neteaseRepository: com.maxrave.data.repository.NeteaseRepositoryImpl = koinInject(),
    mediaPlayerHandler: MediaPlayerHandler = koinInject(),
    navController: NavController,
    isExpanded: Boolean,
    dismissIcon: ImageVector,
    onDismiss: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    // 播放页销毁时清掉 VM(Koin single,常驻)持有的封面位图引用:它与 pager 的 coil 内存
    // 缓存条目是同一对象,不清的话这张 ~4.4MiB 的 1080 位图被 single VM 全程 pin 住,
    // LRU 驱逐也放不掉。重开播放页时 PlayerPageArtwork/AM backdrop 的 onSuccess 会从
    // 内存缓存即时补喂,导出/分享歌词按钮的可用性随之恢复。
    DisposableEffect(Unit) {
        onDispose { sharedViewModel.setBitmap(null) }
    }

    // ViewModel State
    val controllerState by sharedViewModel.controllerState.collectAsStateWithLifecycle()
    val screenDataState by sharedViewModel.nowPlayingScreenData.collectAsStateWithLifecycle()
    // (perf) The 50 ms position flow is NOT collected here: a snapshot at shell level re-ran the
    // whole sheet — state construction included — twenty times a second. It is consumed inside the
    // two effects below and inside each style's playback controls instead.
    // Audio-delay correction, read here and applied ONLY to the lyric line below. The seek bar and
    // the elapsed-time readout keep the raw position: they report where the player is, while a
    // lyric reports what the ear is hearing, and those two are what the offset separates.
    val lyricsOffsetMs by sharedViewModel.getLyricsOffsetMs().collectAsStateWithLifecycle(0)
    val castState by sharedViewModel.castState.collectAsStateWithLifecycle()
 val remoteLikeState by sharedViewModel.remoteSongLikeState.collectAsStateWithLifecycle()
    val neteaseLoggedIn by sharedViewModel.neteaseLoggedIn.collectAsStateWithLifecycle()
    // Apple Music style's progress-bar codec badge — see NowPlayingContentState.toAudioCodecLabel.
   // Apple Music style's quality line under the progress bar — see NowPlayingContentState.toAudioQualityLabel.
 val formatState by sharedViewModel.format.collectAsStateWithLifecycle(initialValue = null)

    val shouldShowVideo by sharedViewModel.getVideo.collectAsStateWithLifecycle()
    val translatedVoteState by sharedViewModel.translatedVoteState.collectAsStateWithLifecycle()
    val lyricsVoteState by sharedViewModel.lyricsVoteState.collectAsStateWithLifecycle()
    val isUserLoggedIn by sharedViewModel
        .isUserLoggedInFlow()
        .collectAsStateWithLifecycle(initialValue = false)
    val likeStatus by sharedViewModel.likeStatus.collectAsStateWithLifecycle()
    val isDislikePending by sharedViewModel.isDislikePending.collectAsStateWithLifecycle()

    // Which Now Playing style renders the content layer (Settings → Now Playing style).
    val nowPlayingStyle by sharedViewModel
        .getNowPlayingStyle()
        .collectAsStateWithLifecycle(initialValue = DataStoreManager.NOW_PLAYING_STYLE_SPOTIFY)

    // Artwork Pager state — Spotify-style horizontal swipe between queue tracks.
    // The pager wraps the Canvas + Thumbnail layers. Controller layout below stays fixed.
    val nowPlayingState by sharedViewModel.nowPlayingState.collectAsStateWithLifecycle()
    val cloudMutationBus: LibraryMutationBus = koinInject()
    val queueDataState by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    // 云盘评论拦截提示(组合期解析,回调里不能取 stringResource)
    val cloudCommentsUnavailableHint = stringResource(Res.string.cloud_comments_unavailable)
    val artworkQueue by remember {
        derivedStateOf { queueDataState?.data?.listTracks ?: emptyList() }
    }
    // 稳定页 key(见 NowPlayingContentState.artworkPageKeys 注释):重排/洗牌时页面按歌
    // 复用而不是按位置换内容,当前页不会闪成别的歌
    val artworkPageKeys by remember {
        derivedStateOf {
            val counts = HashMap<String, Int>()
            artworkQueue.map { track ->
                val n = counts.getOrDefault(track.videoId, 0)
                counts[track.videoId] = n + 1
                "artwork_${track.videoId}#$n"
            }
        }
    }
    // ⚠️ Use track.videoId (already prefix-stripped at MediaServiceHandlerImpl.kt:386).
    // Do NOT use mediaItem.mediaId — it carries the "Video" prefix for video items.
    // track 在装队列清空-回填窗口会短暂为 null 且被 VM 的 distinctUntilChangedBy(videoId)
    // 拦住不再回发——songEntity 与 track 同指当前曲,回退取它(否则 isNeteaseSong/红心门控
    // 在冷启恢复队列等场景全部误判成 YT)。
    val nowPlayingVideoId: String? = nowPlayingState?.track?.videoId ?: nowPlayingState?.songEntity?.videoId
    // 当前曲是网易 MV(队列/媒体 id 带 NETEASE_MV_ 哨兵):强制渲染视频 + 评论走 R_MV_5_ 线程
    val isNeteaseMv = nowPlayingVideoId?.startsWith(NETEASE_MV_MEDIA_ID_PREFIX) == true
    val isCloudDiskQueue = queueDataState?.data?.playlistId == NETEASE_CLOUD_DISK_PLAYLIST_ID
    // 当前曲在队列里的行(完整挂载/云盘归属/私人文件判定共用;重建窗口可能查不到)
    val currentCloudQueueRow =
        queueDataState?.data?.listTracks?.firstOrNull { it.videoId == nowPlayingVideoId }
    // 完整挂载=当前曲带真实艺人 id(云盘三形状的①);非完整挂载/播客:歌词+一起听
    // 不可用(2026-10-10 用户定:置灰且不可点,上滑手势静默拦截)
    val cloudFullyMounted =
        isCloudDiskQueue &&
            currentCloudQueueRow?.artists
                ?.any { (it.id?.toLongOrNull() ?: 0L) > 0L } == true
    // 云盘挂载显式标记(core 在剥 id 前按原始形状打):非 null=当前曲确属云盘,
    // false=私人文件(未匹配)。不能拿队列行 artists 形状推断——catalog 重建会给
    // 空艺人行回填"Various Artists",切歌推进到的私人文件形状已被污染,只有这个
    // 标记随 Track.copy 存活重建、随序列化存活持久化。null 还有第三种来源:旧
    // 持久化队列(升级前保存)或后来插入队列的普通歌曲——后者本就不属云盘。
    val cloudDiskMountFlag = currentCloudQueueRow?.neteaseCloudDiskMounted
    // 队列的云盘标记纪元(core 在 setQueueData 时定型、恢复时按持久值还原,续批/移除
    // 不翻动,见字段注释):true=标记纪元队列;false=升级前保存的旧快照。旧快照的
    // null 行没有可靠逐行归属,但"恢复后新插入"可精确区分——null 行判定三段:
    // 纪元队列=插入普通歌;非纪元且在恢复原始行注册表=原始行按云盘歌对待(①②
    // 菜单门控/形状启发式保守判私人);非纪元且不在注册表=恢复后插入的普通歌
    val cloudQueueIsFlagEra = queueDataState?.data?.cloudDiskFlagEra == true
    val cloudRestoredOriginalIds = queueDataState?.data?.cloudDiskRestoredOriginalIds ?: emptySet()
    val isCloudDiskSongForMenu =
        isCloudDiskQueue &&
            (
                cloudDiskMountFlag != null ||
                    (!cloudQueueIsFlagEra && nowPlayingVideoId in cloudRestoredOriginalIds)
            )
    // 标记缺席时的私人文件形状补判——只在**非纪元队列**(旧快照,null 行=大概率
    // 云盘歌)生效:纪元队列(页面装载/恢复回填)的 null 行=后来插入的普通歌或归属
    // 未知的原始行,按形状断私人会把元数据残缺的普通歌错锁出写入口。私人文件
    // durationSeconds 恒 0(③ dt=0/兜底构造 0),①②与插入的普通歌都有真实时长;
    // ⚠️ 必须先要求找到队列行——安全调用对 null 行也返回 true,重建窗口查不到
    // 当前曲时会把"身份未知"误读成私人形状
    val cloudLegacyPrivateShape =
        currentCloudQueueRow != null &&
            (currentCloudQueueRow.durationSeconds == 0 || currentCloudQueueRow.artists.isNullOrEmpty())
    // 私人文件:标记 false 直判;标记缺席的形状补判只对**旧快照的原始行**(非纪元
    // 且在恢复注册表)生效——纪元队列的 null 行=插入普通歌,恢复后插入的普通歌
    // 也不在注册表,都不吃形状断言(元数据残缺的普通歌不该被锁出写入口)
    val cloudPrivateFileNow =
        isCloudDiskQueue &&
            (
                cloudDiskMountFlag == false ||
                    (
                        cloudDiskMountFlag == null &&
                            !cloudQueueIsFlagEra &&
                            nowPlayingVideoId in cloudRestoredOriginalIds &&
                            (cloudLegacyPrivateShape || nowPlayingState?.songEntity?.artistName.isNullOrEmpty())
                    )
            )
    // 播客队列的早期判定(isPodcastEpisode 在后面才算,这里手势/按钮拦截先用队列指纹)
    val podcastQueueNow = mediaPlayerHandler.queueData.value?.data?.isNeteasePodcastQueue == true
    val cloudFeaturesBlocked = (isCloudDiskQueue && !cloudFullyMounted) || podcastQueueNow
    // currentOrderIndex() is a plain getter over the player, NOT Compose state, so it is read
    // inside this remember block — whose keys (the queue, and the track now playing) are exactly
    // the two things that can move the player's position. nowPlayingState is published FROM the
    // player's own transition callback, so by the time nowPlayingVideoId changes here the player
    // index has already moved; reading it any earlier would sample the outgoing track.
    val currentOrderIndex by remember(artworkQueue, nowPlayingVideoId) {
        derivedStateOf {
            deriveOrderIndex(
                queue = artworkQueue,
                nowPlayingVideoId = nowPlayingVideoId,
                playerOrderIndex = mediaPlayerHandler.currentOrderIndex(),
            )
        }
    }
    // One PagerState for artwork. A finger moves it directly; transport actions animate it.
    val artworkPagerState =
        rememberPagerState(
            initialPage = currentOrderIndex.coerceAtLeast(0),
            pageCount = { artworkQueue.size.coerceAtLeast(1) },
        )
    // Commit metadata/palette only after the cover movement has stopped.
    var visualOrderIndex by remember { mutableIntStateOf(currentOrderIndex.coerceAtLeast(0)) }
    var isAnimatingFromPlayer by remember { mutableStateOf(false) }
    var artworkDragInProgress by remember { mutableStateOf(false) }
    // After release, the artwork is already headed for the requested page. Hold that destination
    // until Media3 acknowledges the skip; otherwise its old index would animate the Pager backward.
    var pendingArtworkSeek by remember { mutableStateOf<Pair<Int, String?>?>(null) }
    LaunchedEffect(currentOrderIndex, nowPlayingVideoId, artworkDragInProgress, pendingArtworkSeek) {
        if (artworkDragInProgress) return@LaunchedEffect
        pendingArtworkSeek?.let { (requestedIndex, sourceVideoId) ->
            when {
                currentOrderIndex == requestedIndex || nowPlayingVideoId != sourceVideoId -> {
                    pendingArtworkSeek = null
                }
                else -> return@LaunchedEffect
            }
        }
        val target = currentOrderIndex
        // settledPage may still name the old song while an animation heads elsewhere. Only the
        // current destination can suppress a new player transition (rapid reverse/next presses).
        if (artworkPagerState.targetPage == target) return@LaunchedEffect
        if (artworkQueue.isNotEmpty() && target in 0 until artworkQueue.size) {
            isAnimatingFromPlayer = true
            try {
                // Animate only a neighbouring page — that is a track change, and the slide IS the
                // feedback. A jump of many pages is not: trimming a radio queue's history drops the
                // current track's index by dozens without changing the track, and animating that
                // flings the pager through dozens of covers to land on the same song.
                if (abs(target - artworkPagerState.currentPage) <= 1) {
                    artworkPagerState.animateScrollToPage(
                        target,
                        animationSpec = ArtworkPagerSnapAnimation,
                    )
                } else {
                    artworkPagerState.scrollToPage(target)
                }
            } finally {
                isAnimatingFromPlayer = false
            }
        }
    }

    LaunchedEffect(pendingArtworkSeek) {
        val expected = pendingArtworkSeek ?: return@LaunchedEffect
        delay(3_000)
        if (pendingArtworkSeek == expected) pendingArtworkSeek = null
    }

    // settledPage can update before the final motion frame. Keep the visual index on the old
    // song until both the page is stationary and the player has reached that song.
    LaunchedEffect(artworkPagerState, currentOrderIndex, artworkDragInProgress) {
        snapshotFlow {
            artworkPagerState.settledPage to artworkPagerState.isScrollInProgress
        }
            .distinctUntilChanged()
            .collect { (settled, scrolling) ->
                if (!scrolling && !artworkDragInProgress && settled == currentOrderIndex) {
                    visualOrderIndex = settled
                }
            }
    }

    // Queue mutation guard — when queue shrinks below currentPage, scroll to last index
    // to avoid IndexOutOfBoundsException during recomposition.
    LaunchedEffect(artworkQueue.size) {
        if (artworkQueue.isNotEmpty() && artworkPagerState.currentPage >= artworkQueue.size) {
            runCatching { artworkPagerState.scrollToPage(artworkQueue.lastIndex) }
        }
    }

    // State
    val isInPipMode = rememberIsInPipMode()

    val mainScrollState = rememberScrollState()

    var showHideMiddleLayout by rememberSaveable {
        mutableStateOf(true)
    }

    var showSheet by rememberSaveable {
        mutableStateOf(false)
    }
    // 云盘当前曲删除确认目标(播放页三点;不可逆)
    var pendingCloudDeleteId by remember { mutableStateOf<String?>(null) }

    var showFullscreenLyrics by rememberSaveable {
        mutableStateOf(false)
    }
    val fullscreenLyricsRequested by sharedViewModel.fullscreenLyricsRequest.collectAsStateWithLifecycle()
    LaunchedEffect(fullscreenLyricsRequested, cloudFeaturesBlocked) {
        if (fullscreenLyricsRequested) {
            if (!cloudFeaturesBlocked) showFullscreenLyrics = true
            sharedViewModel.consumeFullscreenLyricsRequest()
        }
    }

    var showQueueBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
    // A queue row's ⋯ in the Apple Music style, whose queue is a tab of this screen rather than a
    // sheet: its position and the track that was there. The other styles' queue sheet hosts its own.
    var queueMoreFor by remember { mutableStateOf<Pair<Int, Track>?>(null) }
    // The song Add lyrics was opened for, as its id, title and artist. Typing a song's lyrics by hand
    // can outlast the song, and Save must still go to this one; saveable, because a rotation or a
    // theme change recreates the activity mid-typing.
    var addLyricsFor by rememberSaveable { mutableStateOf<List<String>?>(null) }

    var showInfoBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }
    var showNeteaseComments by rememberSaveable {
        mutableStateOf(false)
    }

    var showCommentsSheet by rememberSaveable {
        mutableStateOf(false)
    }
    var commentDraft by rememberSaveable { mutableStateOf("") }

    var showVoteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // NEW: Add to Playlist state
    var showAddToPlaylistDirectly by rememberSaveable {
        mutableStateOf(false)
    }

    var shouldShowToolbar by remember {
        mutableStateOf(false)
    }

    // Palette state
    val paletteState = rememberPaletteState()

    val startColor =
        remember {
            Animatable(Color.Black)
        }
    val endColor =
        remember {
            Animatable(Color.Black)
        }
    val gradientOffset by remember {
        mutableStateOf(GradientOffset(GradientAngle.CW135))
    }

    var spotShadowColor by remember {
        mutableStateOf(Color.White)
    }

    LaunchedEffect(screenDataState) {
        // Only a canvas that fills the page replaces it; animated artwork sits at its top.
        showHideMiddleLayout = screenDataState.fullscreenCanvas() == null
    }

    // Palette generation lives in its own NEVER-restarting effect. Keyed on screenDataState it
    // was cancelled mid-generate every time ANOTHER field of the data class arrived (canvasData,
    // lyrics, songInfo), and kmpalette parks on Loading when generate() is cancelled — palette
    // stays null, startColor stays black, and the M3E style falls back to the app seed (the
    // "canvas songs are always cyan" bug). Canvas songs hit this deterministically because the
    // canvas fetch always lands after the artwork bitmap.
    LaunchedEffect(Unit) {
        snapshotFlow { screenDataState.bitmap }
            .filterNotNull()
            .distinctUntilChanged()
            .collectLatest {
                paletteState.generate(it)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { paletteState.palette }
            // kmpalette 在重新 generate(缓存未命中)时先把状态置 Loading——palette 属性随之为
            // null。null 若照发,getColorFromPalette() 回落 Color.Black,背景会先向黑动画、新
            // 调色板落地后再弹回:切歌瞬间整页发暗"跳一下"的根源(三主题共用本管线,三主题
            // 都跳的原因)。过滤掉 null,Loading 期间保持上一首的颜色,新调色板落地后一次
            // 平滑过渡。
            .filterNotNull()
            .distinctUntilChanged()
            .collectLatest {
                spotShadowColor = it.getColorFromPalette()
                startColor.animateTo(it.getColorFromPalette())
                // Lands on the same backdrop colour the fade and the area below the gradient
                // use, so the palette ramp resolves into the surface instead of a black patch.
                endColor.animateTo(PlayerBackdropColor)
            }
    }

    // (perf) slider drag state moved into each style's playback controls — the shell no longer
    // tracks the slider, so a 50 ms tick never reaches this file's composition at all.

    // (perf) isCrossfading rides the 50 ms TimeLine object; mapped+distinct it only recomposes
    // this colour when a crossfade actually starts or ends. More importantly, the infinite hue
    // loop below only EXISTS while crossfading: keeping it composed while idle read an animation
    // state on every vsync and invalidated this whole player scope even though the selected target
    // colour was always white.
    val isCrossfading by remember {
        sharedViewModel.timeline.map { it.isCrossfading }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(false)
    val crossfadeTargetColor =
        if (isCrossfading) {
            val infiniteTransition = rememberInfiniteTransition(label = "crossfadeRainbow")
            val rainbowHue by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "rainbowHue",
            )
            hsvToColor(rainbowHue, 1f, 1f)
        } else {
            Color.White
        }
    val sliderTrackColor by animateColorAsState(
        // Preserve the old 300 ms settle back to white after the active loop leaves composition.
        targetValue = crossfadeTargetColor,
        animationSpec = tween(300),
        label = "sliderCrossfadeColor",
    )

    // Show ControlLayout Or Show Artist Badge
    var showHideControlLayout by rememberSaveable {
        mutableStateOf(true)
    }
    val controlLayoutAlpha: Float by animateFloatAsState(
        targetValue = if (showHideControlLayout) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 500,
                easing = LinearEasing,
            ),
        label = "ControlLayoutAlpha",
    )

    var showHideJob by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(key1 = showHideJob) {
        if (!showHideJob) {
            delay(5000)
            if (mainScrollState.value == 0) showHideControlLayout = false
            showHideJob = true
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            screenDataState
        }.distinctUntilChangedBy {
            it.canvasData?.url
        }.collectLatest {
            if (it.fullscreenCanvas() != null && mainScrollState.value == 0) {
                showHideJob = false
            } else {
                showHideJob = true
                showHideControlLayout = true
            }
        }
    }

    LaunchedEffect(key1 = showHideControlLayout) {
        if (showHideControlLayout && screenDataState.fullscreenCanvas() != null && mainScrollState.value == 0) {
            showHideJob = false
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { mainScrollState.value }
            .distinctUntilChanged()
            .collect {
                if (it > 0 && !showHideControlLayout && screenDataState.fullscreenCanvas() != null) {
                    showHideJob = true
                    showHideControlLayout = true
                } else if (showHideControlLayout && it == 0 && screenDataState.fullscreenCanvas() != null) {
                    showHideJob = false
                }
            }
    }

    var currentLyricLineIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }

    // Canvas subtitle sync
    // (perf) The 50 ms position is consumed INSIDE the coroutine — a timelineState key here
    // cancelled and relaunched this effect, and the shell, twenty times a second.
    LaunchedEffect(screenDataState.lyricsData?.lyrics, lyricsOffsetMs) {
        val lyrics = screenDataState.lyricsData?.lyrics
        if (lyrics == null || lyrics.syncType == "UNSYNCED" || lyrics.syncType == null) {
            currentLyricLineIndex = -1
            return@LaunchedEffect
        }
        val lines = lyrics.lines ?: return@LaunchedEffect
        val translatedLines =
            screenDataState.lyricsData
                ?.translatedLyrics
                ?.first
                ?.lines
        // What the ear is hearing right now, which is what a lyric answers to. The offset is a
        // key above, so dragging the setting while paused still moves the line.
        sharedViewModel.timeline.collect { timeline ->
            val nowMs = timeline.current - lyricsOffsetMs
            if (nowMs > 0L) {
                lines.indices.forEach { i ->
                    val startTimeMs = lines[i].startTimeMs.toLongOrNull() ?: 0L
                    val endTimeMs =
                        if (i < lines.size - 1) {
                            lines[i + 1].startTimeMs.toLongOrNull() ?: 0L
                        } else {
                            startTimeMs + 60000
                        }
                    if (nowMs in startTimeMs..endTimeMs) {
                        currentLyricLineIndex = i
                    }
                }
                if (lines.isNotEmpty() &&
                    nowMs in 0..(lines.getOrNull(0)?.startTimeMs?.toLongOrNull() ?: 0L)
                ) {
                    currentLyricLineIndex = -1
                }
            } else {
                currentLyricLineIndex = -1
            }
        }
    }

    if (screenDataState.lyricsData != null && controllerState.isPlaying) {
        KeepScreenOn()
    }
    val playingProgramId = mediaPlayerHandler.nowPlayingState.value.track?.neteaseProgramId
        ?: mediaPlayerHandler.nowPlayingState.value.songEntity?.neteaseProgramId
        ?: mediaPlayerHandler.queueData.value?.data?.listTracks?.firstOrNull { it.videoId == nowPlayingVideoId }?.neteaseProgramId
    val isPodcastEpisode = mediaPlayerHandler.queueData.value?.data?.isNeteasePodcastQueue == true || playingProgramId != null
    // The comments sheet reads the id off the media item instead: track is null whenever the item
    // was not in the queue list at the moment it started, and comments exist for every track.
    val commentsVideoId: String? =
        nowPlayingState
            ?.mediaItem
            ?.let { if (it.isVideo()) it.mediaId.removePrefix(MERGING_DATA_TYPE.VIDEO) else it.mediaId }
            ?.takeIf { it.isNotEmpty() }

    val state =
        NowPlayingContentState(
            screenData = screenDataState,
            controllerState = controllerState,
            timelineFlow = sharedViewModel.timeline,
            castState = castState,
            // MV:看 MV 语义即视频常显,不受"视频代替音频"设置门控
            shouldShowVideo = shouldShowVideo || isNeteaseMv ||
                com.maxrave.domain.data.player.ExplicitVideoPlayback.includes(nowPlayingVideoId),
            isNeteaseSong = nowPlayingVideoId?.toLongOrNull() != null || isNeteaseMv,
            isPodcastSong = isPodcastEpisode,
            isNeteaseMv = isNeteaseMv,
            isCloudDiskQueue = isCloudDiskQueue,
            cloudFeaturesBlocked = cloudFeaturesBlocked || isPodcastEpisode,
            cloudFullyMounted = cloudFullyMounted,
            cloudLyricsBlocked = isCloudDiskQueue && !cloudFullyMounted,
            // 私人文件(写类语义未验证):三主题快捷"加歌单"置灰——与三点菜单
            // 的 isCloudPrivateFile 同一判据(见 cloudPrivateFileNow 注释)
            cloudPrivateFile = cloudPrivateFileNow,
            remoteLikeState = remoteLikeState,
            // 红心=云端态;未登录源置灰(点击提示登录);MV 红心=收藏,吃网易登录态
            likeEnabled =
                if (isPodcastEpisode) {
                    neteaseLoggedIn && remoteLikeState.videoId == nowPlayingVideoId && remoteLikeState.liked != null &&
                        !remoteLikeState.pending && playingProgramId != null
                } else if (nowPlayingVideoId?.toLongOrNull() != null || isNeteaseMv) {
                    neteaseLoggedIn
                } else {
                    isUserLoggedIn
                },
            isUserLoggedIn = isUserLoggedIn,
            artworkQueue = artworkQueue,
            artworkPageKeys = artworkPageKeys,
            currentOrderIndex = currentOrderIndex,
            visualOrderIndex = visualOrderIndex,
            artworkPagerState = artworkPagerState,
            artworkDragInProgress = artworkDragInProgress,
            artworkMotionInProgress =
                artworkDragInProgress || pendingArtworkSeek != null ||
                    isAnimatingFromPlayer || artworkPagerState.isScrollInProgress,
            startColor = startColor,
            endColor = endColor,
            spotShadowColor = spotShadowColor,
            gradientOffset = gradientOffset,
            sliderTrackColor = sliderTrackColor,
            currentLyricLineIndex = currentLyricLineIndex,
            showControlLayout = showHideControlLayout,
            controlLayoutAlpha = controlLayoutAlpha,
            showHideMiddleLayout = showHideMiddleLayout,
            shouldShowToolbar = shouldShowToolbar,
            isInPipMode = isInPipMode,
            mainScrollState = mainScrollState,
            isExpanded = isExpanded,
            dismissIcon = dismissIcon,
         // codecs, NOT mimeType. StreamRepositoryImpl splits YouTube's
            // `audio/webm; codecs="opus"` with a regex and stores the two halves in SEPARATE
            // columns: mimeType keeps "audio/webm", codecs keeps "opus". Asking mimeType for
            // the codec therefore never matched anything and the badge never rendered, on any track.
            audioCodecLabel = formatState?.codecs.toAudioCodecLabel(),
           audioQualityLabel = formatState.toAudioQualityLabel(),
            lyricsOffsetMs = lyricsOffsetMs.toLong(),
            videoAspectRatio = rememberVideoAspectRatio(MAIN_PLAYER) ?: 16f / 9,
            videoId = commentsVideoId,
            isPullingDown = isPullingDown,
    )
    // 锁窗内上滑转发注册:把整页列表的 dispatchRawDelta 交上去(dy 取反=手指上移列表下滚)
    LaunchedEffect(state) {
        onUpDragSinkReady { delta -> state.mainScrollState.dispatchRawDelta(-delta) }
    }
    val actions =
        NowPlayingContentActions(
            onUIEvent = { sharedViewModel.onUIEvent(it) },
            onArtworkDragChanged = { artworkDragInProgress = it },
            onArtworkSkipRequested = { target ->
                pendingArtworkSeek = target to nowPlayingVideoId
            },
            onSeekToQueueIndex = { index ->
                mediaPlayerHandler.playMediaItemInMediaSource(index)
            },
            onArtworkBitmap = { sharedViewModel.setBitmap(it) },
            // Spotify 主题翻页瞬间把 startColor 对齐滑入页正在显示的按页色(见 actions 字段注释);
            // 调色板重新生成落地后,动画从用户正看着的颜色平滑走向新色,不再闪上一首的旧色。
            onSnapPaletteColor = { color ->
                coroutineScope.launch {
                    startColor.animateTo(color, animationSpec = tween(220))
                    endColor.animateTo(PlayerBackdropColor, animationSpec = tween(220))
                }
            },
            onToggleControls = {
                showHideJob = true
                showHideControlLayout = !showHideControlLayout
            },
            // 云盘队列:评论端点对云盘 id 无效(探针同族结论),拦在壳层统一提示,
            // 三主题入口只负责置灰外观——点击都会走到这里
            onShowNeteaseComments = {
                // 非完整挂载云盘歌才拦(完整挂载=真实曲库 id,评论区可用);
                // 播客评论走 A_DJ_1_ 线程本就支持,不拦
                if (isCloudDiskQueue && !cloudFullyMounted) {
                    sharedViewModel.makeToast(cloudCommentsUnavailableHint)
                } else {
                    showNeteaseComments = true
                }
            },
            // YT 评论(上游 InnerTube sheet):入口三主题统一,网易走上面的 M7 sheet;
            // 打开前清草稿(SoundCloud 时刻评论共用同一个 sheet)
            onShowComments = {
                commentDraft = ""
                showCommentsSheet = true
            },
            onNavigateToArtist = {
                // 非完整挂载云盘歌/播客:艺人页无目标,播放页艺人名点击不跳转
                val song = sharedViewModel.nowPlayingState.value?.songEntity
                if (!cloudFeaturesBlocked) {
                    (
                        song?.artistId?.firstOrNull()?.takeIf { it.isNotEmpty() }
                            ?: screenDataState.songInfoData?.authorId
                        )?.let { channelId ->
                            onDismiss()
                            navController.navigate(
                                ArtistDestination(
                                    channelId = channelId,
                                ),
                            )
                    }
                }
            },
           onOpenListenTogether = {
                // 非完整挂载云盘歌/播客:一起听不可用(按钮置灰,此处兜底拦其它入口)
                if (!cloudFeaturesBlocked) {
                    onDismiss()
                    navController.navigate(ListenTogetherDestination)
                }
            },
         onShowMoreSheet = { showSheet = true },
            onShowQueue = { showQueueBottomSheet = true },
            onShowInfo = { showInfoBottomSheet = true },
            // 私人文件与三点菜单同门控(写类语义未验证,按钮已置灰,此处兜底拦
            // 其它触发路径);正常歌曲不受影响
            onShowAddToPlaylist = {
                if (!cloudPrivateFileNow) showAddToPlaylistDirectly = true
            },
            onSetRemoteLiked = sharedViewModel::setRemoteSongLiked,
            onLoginRequired = sharedViewModel::notifyFavoriteNeedsLogin,
            // SoundCloud 主题的时刻评论:带草稿打开同一个评论 sheet
            onWriteComment = { draft ->
                commentDraft = draft
                showCommentsSheet = true
            },
            onShowFullscreenLyrics = {
                // 非完整挂载云盘歌/播客:无歌词,按钮置灰,此处兜底拦(无动作无提示)
                if (!cloudFeaturesBlocked) showFullscreenLyrics = true
            },
            onShowVoteDialog = { showVoteDialog = true },
            onLyricsOffsetChange = { sharedViewModel.setLyricsOffsetMs(it) },
            onEnterFullscreenVideo = {
                onDismiss()
                navController.navigate(FullscreenDestination)
            },
            onDismiss = onDismiss,
            onToolbarVisibilityChange = { shouldShowToolbar = it },
            onMoveQueueItem = { from, to ->
                coroutineScope.launch {
                    mediaPlayerHandler.swap(from, to)
                }
            },
            onRemoveQueueItem = { index ->
                mediaPlayerHandler.removeMediaItem(index)
            },
            onQueueItemMore = { index, track -> queueMoreFor = index to track },
        )

    // Below `state`/`actions`: the landscape lyrics layout renders the current style's own track row
    // and playback controls, so it takes the same contract the style content does.
    if (showFullscreenLyrics) {
        if (getPlatform() == Platform.Desktop) {
            // Desktop's Now Playing is a side panel, not a sheet, so the page opens as a Popup over
            // the whole window — the custom title bar included. A Popup that stops below the bar
            // leaves the bar outside it, and a focusable Popup dismisses on any press outside
            // itself, so a click on the bar used to close the page.
            val windowSize = LocalWindowInfo.current.containerSize
            val density = LocalDensity.current
            // DesktopApp makes the window transparent and clips its content to 12dp corners exactly
            // when it draws the custom title bar (both macOS-only). A Popup is a layer of its own
            // that clip never reaches, so the page rounds itself. getScreenSizeInfo() subtracts that
            // same bar, which makes the height difference the one signal commonMain can read.
            val windowIsRounded = windowSize.height > getScreenSizeInfo().hPX
            val popupPositionProvider =
                remember {
                    object : PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize,
                        ): IntOffset = IntOffset.Zero
                    }
                }
            // A Popup appears in a single frame, which reads as a flash over the whole window. The
            // page fades and scales in instead, and a close plays the reverse first: the flag only
            // drops once the exit has finished, or the Popup would leave mid-animation.
            val pageVisibility = remember { MutableTransitionState(false).apply { targetState = true } }
            val closePage = { pageVisibility.targetState = false }
            LaunchedEffect(pageVisibility.currentState, pageVisibility.isIdle) {
                if (pageVisibility.isIdle && !pageVisibility.currentState) {
                    showFullscreenLyrics = false
                }
            }
            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = closePage,
                properties = PopupProperties(focusable = true),
            ) {
                AnimatedVisibility(
                    visibleState = pageVisibility,
                    enter =
                        fadeIn(tween(FULLSCREEN_LYRICS_ENTER_MS)) +
                            scaleIn(tween(FULLSCREEN_LYRICS_ENTER_MS, easing = FastOutSlowInEasing), initialScale = 0.96f),
                    exit =
                        fadeOut(tween(FULLSCREEN_LYRICS_EXIT_MS)) +
                            scaleOut(tween(FULLSCREEN_LYRICS_EXIT_MS, easing = FastOutSlowInEasing), targetScale = 0.96f),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(
                                    with(density) { windowSize.width.toDp() },
                                    with(density) { windowSize.height.toDp() },
                                ).then(
                                    if (windowIsRounded) Modifier.clip(RoundedCornerShape(12.dp)) else Modifier,
                                ),
                    ) {
                        FullscreenLyricsContent(
                            sharedViewModel = sharedViewModel,
                            navController = navController,
                            color = startColor.value,
                            state = state,
                            actions = actions,
                            nowPlayingStyle = nowPlayingStyle,
                            onDismiss = closePage,
                        )
                    }
                }
            }
        } else {
            FullscreenLyricsSheet(
                sharedViewModel = sharedViewModel,
                navController = navController,
                color = startColor.value,
                state = state,
                actions = actions,
                nowPlayingStyle = nowPlayingStyle,
            ) {
                showFullscreenLyrics = false
            }
        }
    }

    // Declared AFTER the lyrics host on purpose. A rotation recreates the activity, so every open
    // sheet re-enters composition in one pass and attaches in declaration order: the landscape
    // lyrics page opens these same sheets (more, queue, add to playlist) on top of itself, and
    // declaring them first would bury them under the page after the device is turned.
    if (showSheet) {
        NowPlayingBottomSheet(
            onDismiss = {
                showSheet = false
            },
            navController = navController,
            onNavigateToOtherScreen = {
                onDismiss()
            },
            song = null, // Auto set now playing
            setSleepTimerEnable = true,
            changeMainLyricsProviderEnable = true,
            // 云盘当前曲删除入口(列表菜单同款行;确认弹窗在壳层,不可逆)。
            // 不可逆操作只认显式挂载标记(行在+标记非空=归属确凿):队列 ID 判定过宽
            // 会把后来插入的普通歌曲误露(拿普通歌曲 id 调 /cloud/del 语义未验证且
            // 可能误删同 id 的云盘条目);元数据形状(duration==0/空 artists)只证明
            // "私人文件形状"不证明"属云盘"——插入云盘队列的普通歌也可能缺时长/艺人,
            // 且重建窗口查不到队列行时形状读数是 null-true 假象。**恢复回填的标记
            // 同样不证明逐行归属**(回填只证明"这是云盘队列的保存",升级前保存可能
            // 混着插入的普通歌)——cloudDiskRestoreBackfilled 队列上一律抑制,从云盘
            // 页重新起播(真页面出处)才恢复。旧队列快照与身份未知的当前曲一律隐藏,
            // 宁可漏出。
            onDeleteFromCloudDisk =
                if (isCloudDiskQueue &&
                    currentCloudQueueRow != null &&
                    cloudDiskMountFlag != null &&
                    queueDataState?.data?.cloudDiskRestoreBackfilled != true
                ) {
                    { pendingCloudDeleteId = nowPlayingVideoId }
                } else {
                    null
                },
            // Offered only once every provider and the offline cache came back empty for this song.
            onAddLyrics =
                nowPlayingState
                    ?.songEntity
                    ?.takeIf { screenDataState.lyricsNotFound && screenDataState.lyricsData == null }
                    ?.let { song ->
                        { addLyricsFor = arrayListOf(song.videoId, song.title, song.artistName?.joinToString(", ").orEmpty()) }
                    },
            youTubeDislike =
                when {
                    !isUserLoggedIn -> null
                    // Fork: 网易歌/MV 无"踩"(RYD 只对 YT 视频)——整行隐藏,防数字 id 误进 YT 点踩 API
                    nowPlayingVideoId == null || nowPlayingVideoId.toLongOrNull() != null || isNeteaseMv -> null
                    likeStatus == YouTubeLikeStatus.DISLIKE -> YouTubeDislikeState.DISLIKED
                    isDislikePending -> YouTubeDislikeState.DISLIKING
                    else -> YouTubeDislikeState.DISLIKE
                },
            onYouTubeDislike = { videoId -> sharedViewModel.addToYouTubeDisliked(videoId) },
            // 三点菜单的"是云盘歌"信号(与删除回调分离,见调用方注释):旧队列快照
            // 删除隐藏但曲库门控照常;标记纪元队列里插入的普通歌按普通歌对待
            isCloudDiskSong = isCloudDiskSongForMenu,
            // 云盘歌曲信息纠正(匹配/解除)落定→发库页回写事件(云盘页在外时库页
            // 入口原地刷新);本页自身无可刷列表,当前曲旧 id 过期属已知边界
            onCloudDiskSongInfoChanged = { ok ->
                if (ok) cloudMutationBus.send(LibraryMutation.CloudDiskChanged)
            },
        )
    }

    // 云盘当前曲删除确认(列表页同款文案;不可逆)
    pendingCloudDeleteId?.let { vid ->
        AlertDialog(
            containerColor = rememberSurfaceDarkColors().container,
            onDismissRequest = { pendingCloudDeleteId = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 库页回写事件等删除落定且成功再发:删除是异步的,提前发
                        // 事件会让库页轻拉抢在服务端可见前拿到旧封面/旧计数
                        sharedViewModel.deleteCloudDiskSong(vid) { ok ->
                            if (ok) cloudMutationBus.send(LibraryMutation.CloudDiskChanged)
                        }
                        pendingCloudDeleteId = null
                    },
                ) {
                    Text(text = stringResource(Res.string.delete), style = typo().labelSmall)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCloudDeleteId = null }) {
                    Text(text = stringResource(Res.string.cancel), style = typo().labelSmall)
                }
            },
            title = {
                Text(
                    text = stringResource(Res.string.delete_from_cloud_disk_title, 1),
                    style = typo().labelSmall,
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.delete_from_cloud_disk_message),
                    style = typo().bodyMedium,
                )
            },
        )
    }

    if (showQueueBottomSheet) {
        QueueBottomSheet(
            onDismiss = {
                showQueueBottomSheet = false
            },
            navController = navController,
            onNavigateToOtherScreen = {
                showQueueBottomSheet = false
                onDismiss()
            },
        )
    }

    queueMoreFor?.let { (index, track) ->
        NowPlayingBottomSheet(
            onDismiss = { queueMoreFor = null },
            navController = navController,
            song = track.toSongEntity(),
            queuePosition =
                QueuePosition(
                    index,
                    track.videoId,
                    artworkQueue.getOrNull(index - 1)?.videoId,
                    artworkQueue.getOrNull(index + 1)?.videoId,
                ),
            // 云盘队列行身份与队列弹窗同口径(见 queueRowIsCloudDisk);播放页当前曲
            // 菜单另有整套出处判定,这里只是队列行入口,不重复那套
            isCloudDiskSong = queueRowIsCloudDisk(queueDataState, track),
            onNavigateToOtherScreen = { onDismiss() },
        )
    }

    if (showInfoBottomSheet) {
        InfoPlayerBottomSheet(
            onDismiss = {
                showInfoBottomSheet = false
            },
        )
    }

    // 网易歌评论列表(详情卡评论数入口);songId 取自当前曲目,面板自身负责分页。
    // 用 songEntity.videoId 而非 track.videoId:后者在部分转场时机为 null,
    // 空串进去分页直接"已经到底了"
    // 播客节目评论线程与歌曲不同(A_DJ_1_<programId> vs R_SO_4_<songId>):用当前
    // mainSong id 在电台节目列表里反查 programId。反查只在"明确播客队列+评论面板
    // 已打开"时进行(CR-23)——旧实现按每首网易歌切歌都拿 album.id 当 radioId 最多
    // 翻 5 页,普通歌曲纯浪费且挤占 byradio 限流队列;队列判定只认前缀(身份三键
    // 持久化后恢复路径带前缀,不再有 SAVED_QUEUE 丢前缀的老问题)。
    // 值语义:null=反查在途 / ""=完成但未命中(只显示失败,绝不写歌曲线程)
    // / A_DJ_1_<id>=命中。队列 Track 带 programId 时直接 O(1) 命中。
    val podcastProgramThreadId =
        produceState<String?>(
            null,
            nowPlayingVideoId,
            isPodcastEpisode,
            playingProgramId,
            showNeteaseComments,
        ) {
            val vid = nowPlayingVideoId ?: return@produceState
            if (!showNeteaseComments || !isPodcastEpisode) {
                return@produceState
            }
            val directId = playingProgramId
            if (directId != null) {
                value = "A_DJ_1_$directId"
                return@produceState
            }
            val radioId =
                mediaPlayerHandler.queueData.value?.data?.listTracks
                    ?.firstOrNull { it.videoId == vid }?.album?.id?.toLongOrNull()
                    ?: run { value = ""; return@produceState }
            var found: Long? = null
            var offset = 0
            // 最多翻 5 页×30=150 期,覆盖绝大多数电台
            for (i in 0 until 5) {
                val page =
                    runCatching {
                        neteaseRepository.getDjRadioProgramsPage(radioId, offset = offset).getOrNull()
                    }.getOrNull() ?: break
                val hit = page.first.firstOrNull { it.mainSongId?.toString() == vid }
                if (hit != null) {
                    found = hit.id
                    break
                }
                if (!page.second) break
                offset += page.first.size
            }
            value = found?.let { "A_DJ_1_$it" } ?: ""
        }
    if (showNeteaseComments) {
        NeteaseCommentsSheet(
            onDismiss = { showNeteaseComments = false },
            songId =
                if (isNeteaseMv) nowPlayingVideoId.orEmpty()
                else mediaPlayerHandler.nowPlayingState.value.songEntity?.videoId ?: nowPlayingVideoId.orEmpty(),
            // MV:详情响应直接带 R_MV_5_<mvid>,零反查；计数由评论线程响应提供。
            threadId =
                if (isNeteaseMv) {
                    screenDataState.neteaseMvData?.commentThreadId ?: ""
                } else {
                    podcastProgramThreadId.value
                },
            isPodcast = isPodcastEpisode,
        )
    }

    // YT 评论(上游 InnerTube):按当前曲 id 打开;时间戳点击跳转进度
    if (showCommentsSheet) {
        val videoId = nowPlayingVideoId
        if (videoId == null) {
            // 没在播:丢掉请求,别把它挂到下一首上
            LaunchedEffect(Unit) { showCommentsSheet = false }
        } else {
            CommentsBottomSheet(
                videoId = videoId,
                isLoggedIn = isUserLoggedIn,
                onTimestamp = { raw ->
                    // 与 Info 卡描述同一规则:只有落在曲目内的时间才 seek
                    val timestamp = parseTimestampToMilliseconds(raw)
                    val total = sharedViewModel.timeline.value.total
                    if (timestamp != 0.0 && timestamp < total) {
                        sharedViewModel.onUIEvent(UIEvent.UpdateProgress(((timestamp * 100) / total).toFloat()))
                    }
                },
                onLogIn = {
                    showCommentsSheet = false
                    navController.navigate(LoginDestination)
                },
                onDismiss = { showCommentsSheet = false },
                initialDraft = commentDraft,
            )
        }
    }

    addLyricsFor?.let { (videoId, title, artist) ->
        AddLyricsSheet(
            songTitle = title,
            artist = artist,
            onSave = { lyrics -> sharedViewModel.saveUserLyrics(videoId, lyrics) },
            onDismiss = { addLyricsFor = null },
        )
    }

    // NEW: Add to Playlist Bottom Sheet
    if (showAddToPlaylistDirectly) {
        val viewModel: NowPlayingBottomSheetViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.resetPlaylists()
            viewModel.setSongEntity(null) // Uses current playing song
        }

        AddToPlaylistModalBottomSheet(
            isBottomSheetVisible = true,
            listLocalPlaylist = uiState.listLocalPlaylist,
            listYouTubePlaylist = uiState.listYouTubePlaylist,
            listNeteasePlaylist = uiState.listNeteasePlaylist,
            youTubeLoadFailed = uiState.youTubePlaylistsFailed,
            neteaseLoadFailed = uiState.neteasePlaylistsFailed,
            onRetryCloudPlaylists = {
                viewModel.resetPlaylists()
                viewModel.setSongEntity(null)
            },
            onDismiss = { showAddToPlaylistDirectly = false },
            onClick = { playlist ->
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToPlaylist(playlist.id))
                showAddToPlaylistDirectly = false
            },
            onYTPlaylistClick = { playlist ->
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToYouTubePlaylist(playlist.browseId))
                showAddToPlaylistDirectly = false
            },
            onNeteasePlaylistClick = { playlist ->
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToNeteasePlaylist(playlist.browseId))
                showAddToPlaylistDirectly = false
            },
            videoId = uiState.songUIState.videoId,
        )
    }

    // Vote Dialog
    if (showVoteDialog) {
        val canVoteLyrics =
            screenDataState.lyricsData?.lyricsProvider == LyricsProvider.SIMPMUSIC &&
                screenDataState.lyricsData
                    ?.lyrics
                    ?.simpMusicLyrics != null
        val canVoteTranslatedLyrics =
            screenDataState.lyricsData?.translatedLyrics?.second == LyricsProvider.SIMPMUSIC &&
                screenDataState.lyricsData
                    ?.translatedLyrics
                    ?.first
                    ?.simpMusicLyrics != null

        VoteLyricsDialog(
            canVoteLyrics = canVoteLyrics,
            canVoteTranslatedLyrics = canVoteTranslatedLyrics,
            lyricsVoteState = lyricsVoteState,
            translatedLyricsVoteState = translatedVoteState,
            onVoteLyrics = { upvote ->
                sharedViewModel.voteLyrics(upvote)
            },
            onVoteTranslatedLyrics = { upvote ->
                sharedViewModel.voteTranslatedLyrics(upvote)
            },
            onDismiss = {
                showVoteDialog = false
            },
        )
    }

    // 播放页是 ModalBottomSheet(独立 Android 窗口),App 根布局的全局点击观察器看不到
    // 它里面的任何点击 —— 这里把同一套 hapticTapFeedback 旁观观察器挂到播放页根布局,
    // 三主题的播放/暂停/切歌/红心等所有按钮的点击震感由此覆盖(拖动/滑动仍按位移排除,
    // 与根观察器行为一致)。播放页内再弹出的 sheet(队列/信息等)又是新窗口,仍不覆盖。
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .hapticTapFeedback(),
    ) {
        when (nowPlayingStyle) {
            DataStoreManager.NOW_PLAYING_STYLE_M3_EXPRESSIVE ->
                NowPlayingContentM3Expressive(
                    state = state,
                    actions = actions,
                )
            DataStoreManager.NOW_PLAYING_STYLE_APPLE_MUSIC ->
                NowPlayingContentAppleMusic(
                    state = state,
                    actions = actions,
                )

            DataStoreManager.NOW_PLAYING_STYLE_SOUNDCLOUD ->
                NowPlayingContentSoundCloud(
                    state = state,
                    actions = actions,
                )

            else ->
                NowPlayingContentSpotify(
                    state = state,
                    actions = actions,
                )
        }
    }
}
